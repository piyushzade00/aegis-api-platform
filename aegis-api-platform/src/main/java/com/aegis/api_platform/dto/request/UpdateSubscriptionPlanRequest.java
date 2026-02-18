package com.aegis.api_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateSubscriptionPlanRequest(
        @NotBlank(message = "Plan name must not be blank.")
        String name,

        @NotNull(message = "Monthly quota is required.")
        @Positive(message = "Monthly quota must be greater than zero.")
        Long monthlyQuota,

        @NotNull(message = "Rate limit per minute is required.")
        @Positive(message = "Rate limit per minute must be greater than zero.")
        Integer rateLimitPerMinute,

        @NotBlank(message = "Currency must not be blank.")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code.")
        String currency
) {}
