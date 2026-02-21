package com.aegis.api_platform.dto.response;

import com.aegis.api_platform.enums.ApiStatus;
import com.aegis.api_platform.enums.HttpMethod;

import java.time.Instant;

public record ApiResponse(
        Long id,
        Long tenantId,
        String name,
        String path,
        HttpMethod httpMethod,
        String targetUrl,
        String description,
        ApiStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
