package com.aegis.api_platform.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
        @NotBlank(message = "Tenant name cannot be blank")
        String name
) {}
