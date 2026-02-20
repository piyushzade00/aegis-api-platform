package com.aegis.api_platform.exception;

public class MonthlyQuotaExceededException extends RuntimeException {
    public MonthlyQuotaExceededException(String message) {
        super(message);
    }
}
