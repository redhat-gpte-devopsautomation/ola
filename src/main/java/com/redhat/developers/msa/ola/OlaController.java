/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.developers.msa.ola;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerSpan;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.StringServiceNameProvider;
import com.github.kristofa.brave.httpclient.BraveHttpRequestInterceptor;
import com.github.kristofa.brave.httpclient.BraveHttpResponseInterceptor;

import feign.Logger;
import feign.Logger.Level;
import feign.httpclient.ApacheHttpClient;
import feign.hystrix.HystrixFeign;
import feign.jackson.JacksonDecoder;

@RestController
@RequestMapping("/api")
public class OlaController {

    @Autowired
    private Brave brave;

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/ola", produces = "text/plain")
    public String ola() {
        String hostname = System.getenv().getOrDefault("HOSTNAME", "Unknown");
        return String.format("Olá de %s", hostname);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/ola-chaining", produces = "application/json")
    public List<String> sayHelloChaining() {
        List<String> greetings = new ArrayList<>();
        greetings.add(ola());
        greetings.addAll(getNextService().hola());
        return greetings;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/health")
    public String health() {
        return "I'm ok";
    }

    /**
     * This is were the "magic" happens: it creates a Feign, which is a proxy interface for remote calling a REST endpoint with
     * Hystrix fallback support.
     *
     * @return The feign pointing to the service URL and with Hystrix fallback.
     */
    private HolaService getNextService() {
        final String serviceName = "hola";
        // This stores the Original/Parent ServerSpan from ZiPkin.
        final ServerSpan serverSpan = brave.serverSpanThreadBinder().getCurrentServerSpan();
        final CloseableHttpClient httpclient =
            HttpClients.custom()
                .addInterceptorFirst(new BraveHttpRequestInterceptor(brave.clientRequestInterceptor(), new StringServiceNameProvider(serviceName), new DefaultSpanNameProvider()))
                .addInterceptorFirst(new BraveHttpResponseInterceptor(brave.clientResponseInterceptor()))
                .build();
        String url = String.format("http://%s:8080/", serviceName);
        return HystrixFeign.builder()
            // Use apache HttpClient which contains the ZipKin Interceptors
            .client(new ApacheHttpClient(httpclient))
            // Bind Zipkin Server Span to Feign Thread
            .requestInterceptor((t) -> brave.serverSpanThreadBinder().setCurrentSpan(serverSpan))
            .logger(new Logger.ErrorLogger()).logLevel(Level.BASIC)
            .decoder(new JacksonDecoder())
            .target(HolaService.class, url,
                () -> Collections.singletonList("Hola response (fallback)"));
    }

}
