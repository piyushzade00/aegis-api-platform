package com.aegis.api_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AegisApiPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AegisApiPlatformApplication.class, args);
	}

}
