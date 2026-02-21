package com.aegis.api_platform.gateway;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class BackendCallerService {

    @CircuitBreaker(name = "gatewayBackend", fallbackMethod = "fallbackResponse")
    public ResponseEntity<String> call(WebClient.ResponseSpec responseSpec) {
        return responseSpec.toEntity(String.class).block();
    }

    public ResponseEntity<String> fallbackResponse(
            WebClient.ResponseSpec responseSpec,
            Throwable ex
    ) {
        return ResponseEntity
                .status(503)
                .body("Target service unavailable");
    }
}
