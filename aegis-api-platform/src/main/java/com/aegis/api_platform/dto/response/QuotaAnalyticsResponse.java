package com.aegis.api_platform.dto.response;

public record QuotaAnalyticsResponse(
        Long tenantId,
        Long monthlyQuota,
        Long usedThisMonth,
        Long remainingQuota
) {}
