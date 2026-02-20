package com.aegis.api_platform.dto.response;

public record ApiUsageResponse(
        Long apiId,
        Long totalRequests
) {}
