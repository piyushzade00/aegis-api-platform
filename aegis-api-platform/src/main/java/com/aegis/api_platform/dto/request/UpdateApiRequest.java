package com.aegis.api_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateApiRequest(

        @NotBlank(message = "API name cannot be blank")
        String name,

        @NotBlank(message = "Target URL cannot be blank")
        @Pattern(
                regexp = "^(http|https)://.*$",
                message = "Target URL must start with http:// or https://"
        )
        String targetUrl,

        String description,

        boolean isPublic
) {}
