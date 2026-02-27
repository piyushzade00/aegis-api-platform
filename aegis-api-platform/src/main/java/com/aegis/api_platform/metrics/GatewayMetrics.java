package com.aegis.api_platform.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GatewayMetrics {

    private final Counter gatewayRequestCounter;
    private final Counter rateLimitExceededCounter;
    private final Counter monthlyQuotaExceededCounter;

    public GatewayMetrics(MeterRegistry registry) {

        this.gatewayRequestCounter =
                Counter.builder("gateway_request_total")
                        .description("Total gateway requests processed")
                        .register(registry);

        this.rateLimitExceededCounter =
                Counter.builder("rate_limit_exceeded_total")
                        .description("Total rate limit violations")
                        .register(registry);

        this.monthlyQuotaExceededCounter =
                Counter.builder("monthly_quota_exceeded_total")
                        .description("Total monthly quota violations")
                        .register(registry);
    }

    public void incrementGatewayRequest() {
        gatewayRequestCounter.increment();
    }

    public void incrementRateLimitExceeded() {
        rateLimitExceededCounter.increment();
    }

    public void incrementMonthlyQuotaExceeded() {
        monthlyQuotaExceededCounter.increment();
    }
}
