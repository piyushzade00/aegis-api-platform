package com.aegis.api_platform.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateSubscriptionPlanRequest(
        @NotBlank(message = "Plan code must not be blank.")
        @Pattern(
                regexp = "^[A-Z0-9_]+$",
                message = "Plan code must contain only uppercase letters, numbers, or underscores."
        )
        String planCode,

        @NotBlank(message = "Plan name must not be blank.")
        String name,

        @NotNull(message = "Monthly quota is required.")
        @Positive(message = "Monthly quota must be greater than zero.")
        Long monthlyQuota,

        @NotNull(message = "Rate limit per minute is required.")
        @Positive(message = "Rate limit per minute must be greater than zero.")
        Integer rateLimitPerMinute,

        @NotNull(message = "Price is required.")
        @PositiveOrZero(message = "Price must be greater than zero.")
        BigDecimal price,

        @NotBlank(message = "Currency must not be blank.")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code.")
        String currency
) {}
