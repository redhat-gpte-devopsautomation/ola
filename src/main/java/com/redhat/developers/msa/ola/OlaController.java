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

import com.netflix.hystrix.HystrixCommandProperties;
import feign.hystrix.HystrixFeign;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OlaController {

	/**
	 * The next REST endpoint URL of the service chain to be called.
	 */
	private static final String NEXT_ENDPOINT_URL = "http://hola:8080/api/hola-chaining";

	/**
	 * Setting Hystrix timeout for the chain in 750ms (we have 3 more chained service calls).
	 */
	static {
		HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(750);
	}

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
		greetings.addAll(createFeign().greetings());
		return greetings;
	}

	/**
	 * This is were the "magic" happens: it creates a Feign, which is a proxy interface for remote
	 * calling a REST endpoint with Hystrix fallback support.
	 *
	 * @return The feign pointing to the service URL and with Hystrix fallback.
	 */
	private ChainedGreeting createFeign() {
		return HystrixFeign.builder().target(ChainedGreeting.class, NEXT_ENDPOINT_URL,
				() -> Collections.singletonList("Hola response (fallback)"));
	}

}
