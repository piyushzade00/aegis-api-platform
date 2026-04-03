package com.aegis.api_platform.ai.agent;

import com.aegis.api_platform.ai.agent.alert.AlertEvent;
import com.aegis.api_platform.analytics.service.AnalyticsService;
import com.aegis.api_platform.config.RabbitConfig;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;
import com.aegis.api_platform.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentTools {

    private final AnalyticsService analyticsService;
    private final TenantService tenantService;
    private final RabbitTemplate rabbitTemplate;

    @Tool(description = """
        Get quota consumption percentage for a tenant.
        Returns a value between 0 and 100 representing percentage used.
        Use this to check if a tenant is close to their monthly limit.
        """)
    public double getQuotaConsumptionPercent(
            @ToolParam(description = "The tenant ID") Long tenantId) {

        QuotaAnalyticsResponse quota =
                analyticsService.getQuotaStatus(tenantId);

        if (quota.monthlyQuota() == 0) return 0.0;

        return (quota.usedThisMonth() * 100.0) / quota.monthlyQuota();
    }

    @Tool(description = """
        Get list of all active tenant IDs.
        Use this at the start of analysis to know which tenants to check.
        """)
    public List<Long> getActiveTenantIds() {
        return tenantService.getAllActiveTenants();
    }

    @Tool(description = """
        Get daily request count for a tenant for today.
        Use this to detect unusual traffic spikes.
        """)
    public Long getTodayRequestCount(
            @ToolParam(description = "The tenant ID") Long tenantId) {

        Instant startOfDay = LocalDate.now(ZoneOffset.UTC)
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant now = Instant.now();

        return analyticsService.getTotalUsageForTenant(
                tenantId, startOfDay, now);
    }

    @Tool(description = """
        Publish an alert for a tenant anomaly.
        Use this when you detect a quota warning, critical quota,
        or rate limit spike that requires attention.
        alertType must be one of: QUOTA_WARNING, QUOTA_CRITICAL,
        RATE_LIMIT_SPIKE.
        severityScore must be between 0.0 and 1.0.
        """)
    public String publishAlert(
            @ToolParam(description = "The tenant ID") Long tenantId,
            @ToolParam(description = "Alert type") String alertType,
            @ToolParam(description = "Human readable message") String message,
            @ToolParam(description = "Severity 0.0 to 1.0") double severityScore) {

        AlertEvent alert = new AlertEvent(
                UUID.randomUUID().toString(),
                tenantId,
                alertType,
                message,
                severityScore,
                Instant.now()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.ALERT_EXCHANGE,
                RabbitConfig.ALERT_ROUTING_KEY,
                alert
        );

        log.info("Alert published for tenant {}: {} - {}",
                tenantId, alertType, message);

        return "Alert published successfully";
    }
}
