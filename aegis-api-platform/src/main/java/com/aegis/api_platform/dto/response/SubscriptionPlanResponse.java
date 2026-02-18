package com.aegis.api_platform.dto.response;

import com.aegis.api_platform.enums.Status;

import java.math.BigDecimal;
import java.time.Instant;

public record SubscriptionPlanResponse(
        Long id,
        String planCode,
        String name,
        Long monthlyQuota,
        Integer rateLimitPerMinute,
        BigDecimal price,
        String currency,
        Status status,
        Instant createdAt,
        Instant updatedAt
) {}
