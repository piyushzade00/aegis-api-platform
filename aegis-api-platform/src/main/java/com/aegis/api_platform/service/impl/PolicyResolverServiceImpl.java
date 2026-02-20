package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.model.ApiDefinition;
import com.aegis.api_platform.model.PlanApiConfig;
import com.aegis.api_platform.policy.ApiPolicy;
import com.aegis.api_platform.service.PlanApiConfigService;
import com.aegis.api_platform.service.PolicyResolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolicyResolverServiceImpl
        implements PolicyResolverService {

    private final PlanApiConfigService planApiConfigService;

    @Override
    public ApiPolicy resolve(Long planId, ApiDefinition api) {

        Integer effectiveRate =
                api.getTenant()
                        .getSubscriptionPlan()
                        .getRateLimitPerMinute();

        Long effectiveMonthly =
                api.getTenant()
                        .getSubscriptionPlan()
                        .getMonthlyQuota();

        Optional<PlanApiConfig> configOpt =
                planApiConfigService.getConfig(planId, api.getId());

        if (configOpt.isPresent()) {

            PlanApiConfig config = configOpt.get();

            if (config.getRateLimitPerMinuteOverride() != null) {
                effectiveRate =
                        config.getRateLimitPerMinuteOverride();
            }

            if (config.getMonthlyQuotaOverride() != null) {
                effectiveMonthly =
                        config.getMonthlyQuotaOverride();
            }
        }

        return new ApiPolicy(effectiveRate, effectiveMonthly);
    }
}
