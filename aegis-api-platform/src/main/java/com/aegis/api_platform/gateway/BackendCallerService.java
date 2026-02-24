package com.aegis.api_platform.gateway;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class BackendCallerService {

    @CircuitBreaker(name = "gatewayBackend", fallbackMethod = "fallbackResponse")
    public ResponseEntity<String> call(WebClient.ResponseSpec responseSpec) {
        return responseSpec
                .toEntity(String.class)
                .timeout(Duration.ofSeconds(6))
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(200))
                                .filter(this::isRetryable)
                )
                .block();
    }

    public ResponseEntity<String> fallbackResponse(
            WebClient.ResponseSpec responseSpec,
            Throwable ex
    ) {
        return ResponseEntity
                .status(503)
                .body("Target service unavailable");
    }

    private boolean isRetryable(Throwable ex) {

        return ex instanceof TimeoutException
                || ex instanceof ConnectException
                || (ex instanceof WebClientResponseException
                && ((WebClientResponseException) ex)
                .getStatusCode().is5xxServerError());
    }
}
