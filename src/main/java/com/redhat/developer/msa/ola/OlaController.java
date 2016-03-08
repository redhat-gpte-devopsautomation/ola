package com.redhat.developer.msa.ola;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;

@RestController
public class OlaController {

	@RequestMapping(value = "/", produces = "text/plain")
	public String ola() throws UnknownHostException {
		String hostname = System.getenv().getOrDefault("HOSTNAME", "unknown");
		return String.format("Olá de %s", hostname);
	}

}
