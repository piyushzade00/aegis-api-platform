package com.aegis.api_platform.ai.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionAgent {

    private final ChatClient chatClient;
    private final AgentTools agentTools;

    @Value("${ai.agent.quota-warning-threshold:0.8}")
    private double quotaWarningThreshold;

    @Value("${ai.agent.rate-breach-threshold:3}")
    private double rateBreachThreshold;

    @Scheduled(cron = "${ai.agent.schedule:0 0 * * * *}")
    public void runAnomalyDetection() {

        log.info("Anomaly detection agent starting");

        String systemPrompt = buildAgentSystemPrompt();

        try {
            String result = chatClient.prompt()
                    .system(systemPrompt)
                    .user("Analyze all active tenants for anomalies now.")
                    .tools(agentTools)
                    .call()
                    .content();

            log.info("Anomaly detection agent completed: {}", result);

        } catch (Exception ex) {
            log.error("Anomaly detection agent failed", ex);
        }
    }

    private String buildAgentSystemPrompt() {
        return String.format("""
            You are an anomaly detection agent for Aegis API Gateway.

            Your job is to analyze all active tenants and detect anomalies.

            Follow these steps exactly:
            1. Call getActiveTenantIds to get all active tenants.
            2. For each tenant, call getQuotaConsumptionPercent.
            3. For each tenant, call getTodayRequestCount.
            4. Apply these rules:
               - If quota consumption > %.0f%%, publish a QUOTA_WARNING alert.
               - If quota consumption > 95%%, publish a QUOTA_CRITICAL alert.
               - If today's request count > %.0fx the tenant's 30-day daily average,
                 publish a RATE_LIMIT_SPIKE alert.
            5. Only publish an alert if a rule is triggered.
               Do not publish alerts for healthy tenants.
            6. After checking all tenants, summarize what you found.

            Use only the tools provided. Do not invent data.
            """, quotaWarningThreshold * 100, rateBreachThreshold);
    }
}
