package com.aegis.api_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        @NotBlank(message = "Tenant name cannot be blank")
        @Size(max = 100, message = "Tenant name must not exceed 100 characters")
        String name,
        @NotNull(message = "Subscription plan id is required")
        @Positive(message = "Subscription plan id must be a positive number")
        Long subscriptionPlanId
) {}
