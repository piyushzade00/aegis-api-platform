package com.aegis.api_platform.dto.request;

import com.aegis.api_platform.enums.HttpMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateApiRequest(

        @NotBlank(message = "API name cannot be blank")
        String name,

        @NotBlank(message = "Path cannot be blank")
        String path,

        @NotNull(message = "HTTP method is required")
        HttpMethod httpMethod,

        @NotBlank(message = "Target URL cannot be blank")
        @Pattern(
                regexp = "^(http|https)://.*$",
                message = "Target URL must start with http:// or https://"
        )
        String targetUrl,

        String description
) {}
