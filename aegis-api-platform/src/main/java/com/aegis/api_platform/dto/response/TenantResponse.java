package com.aegis.api_platform.dto.response;

import com.aegis.api_platform.enums.Status;

import java.time.Instant;

public record TenantResponse(
        Long id,
        String name,
        Status status,
        Instant createdAt,
        Instant updatedAt
) {}
