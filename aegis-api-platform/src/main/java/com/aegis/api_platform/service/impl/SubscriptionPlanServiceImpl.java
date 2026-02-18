package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.dto.request.CreateSubscriptionPlanRequest;
import com.aegis.api_platform.dto.request.UpdateSubscriptionPlanRequest;
import com.aegis.api_platform.model.SubscriptionPlan;
import com.aegis.api_platform.repository.SubscriptionPlanRepository;
import com.aegis.api_platform.repository.TenantRepository;
import com.aegis.api_platform.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository repository;
    private final TenantRepository tenantRepository;

    @Override
    public SubscriptionPlan createSubscriptionPlan(CreateSubscriptionPlanRequest request) {

        if (repository.existsByPlanCodeIgnoreCase(request.planCode())) {
            throw new IllegalArgumentException("Plan code already exists.");
        }

        if (request.price().compareTo(BigDecimal.ZERO) == 0 && !request.planCode().equals("FREE")) {
            throw new IllegalArgumentException("Only FREE plan can have zero price.");
        }


        SubscriptionPlan plan = new SubscriptionPlan(
                request.planCode().trim(),
                request.name().trim(),
                request.monthlyQuota(),
                request.rateLimitPerMinute(),
                request.price(),
                request.currency().trim()
        );

        return repository.save(plan);
    }

    @Override
    public SubscriptionPlan updateSubscriptionPlan(Long id, UpdateSubscriptionPlanRequest request) {

        SubscriptionPlan plan = getOrThrow(id);

        boolean hasTenants = tenantRepository.existsBySubscriptionPlanId(id);

        if (hasTenants) {
            throw new IllegalStateException("Cannot modify plan assigned to tenants.");
        }

        plan.updateDetails(
                request.name().trim(),
                request.monthlyQuota(),
                request.rateLimitPerMinute(),
                request.currency().trim()
        );

        return plan;
    }

    @Override
    public SubscriptionPlan archiveSubscriptionPlan(Long id) {
        SubscriptionPlan plan = getOrThrow(id);
        plan.archive();
        return plan;
    }

    @Override
    public SubscriptionPlan activateSubscriptionPlan(Long id) {
        SubscriptionPlan plan = getOrThrow(id);
        plan.activate();
        return plan;
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlan getSubscriptionPlanById(Long id) {
        return getOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getAllSubscriptionPlans() {
        return repository.findAll();
    }

    private SubscriptionPlan getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
    }
}
