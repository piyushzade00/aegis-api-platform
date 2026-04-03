package com.aegis.api_platform.exception;

public class AiRateLimitExceededException extends RuntimeException {
    public AiRateLimitExceededException(String message) {
        super(message);
    }
}
