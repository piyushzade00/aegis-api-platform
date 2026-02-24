package com.aegis.api_platform.dto.request;

public record PlanApiConfigRequest(
        Integer rateLimitPerMinuteOverride,
        Long monthlyQuotaOverride
) {}
