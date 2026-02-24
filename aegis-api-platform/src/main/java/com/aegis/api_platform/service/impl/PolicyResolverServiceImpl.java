package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.model.ApiDefinition;
import com.aegis.api_platform.model.PlanApiConfig;
import com.aegis.api_platform.model.SubscriptionPlan;
import com.aegis.api_platform.policy.ApiPolicy;
import com.aegis.api_platform.repository.SubscriptionPlanRepository;
import com.aegis.api_platform.service.PlanApiConfigService;
import com.aegis.api_platform.service.PolicyResolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolicyResolverServiceImpl
        implements PolicyResolverService {

    private final SubscriptionPlanRepository planRepository;
    private final PlanApiConfigService planApiConfigService;

    @Override
    public ApiPolicy resolve(Long planId, Long apiId) {

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        Integer effectiveRate = plan.getRateLimitPerMinute();

        Long effectiveMonthly = plan.getMonthlyQuota();

        Optional<PlanApiConfig> configOpt =
                planApiConfigService.getConfig(planId, apiId);

        if (configOpt.isPresent()) {

            PlanApiConfig config = configOpt.get();

            if (config.getRateLimitPerMinuteOverride() != null) {
                effectiveRate = config.getRateLimitPerMinuteOverride();
            }

            if (config.getMonthlyQuotaOverride() != null) {
                effectiveMonthly = config.getMonthlyQuotaOverride();
            }
        }

        return new ApiPolicy(effectiveRate, effectiveMonthly);
    }
}
