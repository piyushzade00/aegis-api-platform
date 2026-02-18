package com.aegis.api_platform.mapper;

import com.aegis.api_platform.dto.response.SubscriptionPlanResponse;
import com.aegis.api_platform.model.SubscriptionPlan;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getPlanCode(),
                plan.getName(),
                plan.getMonthlyQuota(),
                plan.getRateLimitPerMinute(),
                plan.getPrice(),
                plan.getCurrency(),
                plan.getStatus(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
