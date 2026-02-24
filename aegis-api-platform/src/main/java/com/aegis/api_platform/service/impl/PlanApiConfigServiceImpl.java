package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.model.ApiDefinition;
import com.aegis.api_platform.model.PlanApiConfig;
import com.aegis.api_platform.model.SubscriptionPlan;
import com.aegis.api_platform.repository.ApiDefinitionRepository;
import com.aegis.api_platform.repository.PlanApiConfigRepository;
import com.aegis.api_platform.repository.SubscriptionPlanRepository;
import com.aegis.api_platform.service.PlanApiConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanApiConfigServiceImpl
        implements PlanApiConfigService {

    private final PlanApiConfigRepository configRepository;
    private final SubscriptionPlanRepository planRepository;
    private final ApiDefinitionRepository apiRepository;

    @Override
    public Optional<PlanApiConfig> getConfig(
            Long planId,
            Long apiId
    ) {
        return configRepository.findByPlanIdAndApiId(
                planId,
                apiId
        );
    }

    @Override
    public void createOrUpdate(
            Long planId,
            Long apiId,
            Integer rateOverride,
            Long monthlyOverride
    ) {
        if (rateOverride == null && monthlyOverride == null) {
            throw new IllegalArgumentException(
                    "At least one override must be provided"
            );
        }

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        ApiDefinition api = apiRepository.findById(apiId)
                .orElseThrow(() -> new IllegalArgumentException("API not found"));

        validateOverrides(plan, rateOverride, monthlyOverride);

        PlanApiConfig config = configRepository
                .findByPlanIdAndApiId(planId, apiId)
                .orElse(null);

        if (config == null) {
            config = new PlanApiConfig(
                    plan,
                    api,
                    rateOverride,
                    monthlyOverride
            );
        } else {
            config.setRateLimitPerMinuteOverride(rateOverride);
            config.setMonthlyQuotaOverride(monthlyOverride);
        }

        configRepository.save(config);
    }

    private void validateOverrides(
            SubscriptionPlan plan,
            Integer rateOverride,
            Long monthlyOverride
    ) {

        if (rateOverride != null) {
            if (rateOverride <= 0) {
                throw new IllegalArgumentException("Rate override must be positive");
            }
            if (rateOverride > plan.getRateLimitPerMinute()) {
                throw new IllegalArgumentException("Rate override cannot exceed plan limit");
            }
        }

        if (monthlyOverride != null) {
            if (monthlyOverride <= 0) {
                throw new IllegalArgumentException("Monthly override must be positive");
            }
            if (monthlyOverride > plan.getMonthlyQuota()) {
                throw new IllegalArgumentException("Monthly override cannot exceed plan limit");
            }
        }
    }
}
