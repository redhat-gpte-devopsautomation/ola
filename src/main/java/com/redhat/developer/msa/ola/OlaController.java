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
package com.redhat.developer.msa.ola;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OlaController {

	@CrossOrigin
	@RequestMapping(method = RequestMethod.GET, value = "/ola", produces = "text/plain")
	public String ola() throws UnknownHostException {
		String hostname = System.getenv().getOrDefault("HOSTNAME", "unknown");
		return String.format("Olá de %s", hostname);
	}
	
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/ola-chaining", produces = "text/plain")
    public String sayHelloChaining() {
        JSONArray newArray = new JSONArray();
        try {
            newArray.put(ola());
            String bonJourResponse = getBonjourResponse();
            JSONArray responseArray = new JSONArray(bonJourResponse);
            for (int x=0; x < responseArray.length(); x++){
                newArray.put(responseArray.get(x));
            }
        } catch (Exception e) {
            newArray.put("Error: " + e.getMessage());
        }
        return newArray.toString();
    }

    private String getBonjourResponse() throws IOException {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(2000)
            .setConnectionRequestTimeout(2000)
            .build();
        HttpGet httpGet = new HttpGet("http://hola:8080/rest/hola-chaining");
        httpGet.setConfig(requestConfig);
        HttpClient httpClient = HttpClientBuilder.create().build();
        return EntityUtils.toString(httpClient.execute(httpGet).getEntity());
    }

}
