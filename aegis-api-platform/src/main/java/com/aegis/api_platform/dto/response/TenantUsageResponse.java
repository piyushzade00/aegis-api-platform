package com.aegis.api_platform.dto.response;

public record TenantUsageResponse(
        Long tenantId,
        Long totalRequests
) {}