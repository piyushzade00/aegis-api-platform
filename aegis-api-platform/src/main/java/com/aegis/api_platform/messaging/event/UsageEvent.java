package com.aegis.api_platform.messaging.event;

import java.time.Instant;

public record UsageEvent(
        Long tenantId,
        Long apiId,
        Long apiKeyId,
        int statusCode,
        long latencyMs,
        Instant timestamp,
        String correlationId
) {}
