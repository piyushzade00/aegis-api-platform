package com.aegis.api_platform.ai.agent.alert;

import java.time.Instant;

public record AlertEvent(
        String alertId,
        Long tenantId,
        String alertType,      // QUOTA_WARNING, RATE_LIMIT_SPIKE, QUOTA_CRITICAL
        String message,
        double severityScore,  // 0.0 to 1.0
        Instant timestamp
) {}
