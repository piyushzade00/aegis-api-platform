package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.enums.Status;
import com.aegis.api_platform.model.SubscriptionPlan;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.repository.SubscriptionPlanRepository;
import com.aegis.api_platform.repository.TenantRepository;
import com.aegis.api_platform.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public Tenant createTenant(String name, Long planId) {
        if (tenantRepository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("Tenant name already exists.");
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found."));

        Tenant tenant = new Tenant(name.trim(), Status.ACTIVE,plan);
        return tenantRepository.save(tenant);
    }

    @Override
    public Tenant suspendTenant(Long tenantId) {
        Tenant tenant = getTenantOrThrow(tenantId);

        if (tenant.getStatus() == Status.DELETED) {
            throw new IllegalStateException("Deleted tenant cannot be suspended.");
        }

        if (tenant.getStatus() == Status.SUSPENDED) {
            return tenant;
        }

        tenant.changeStatus(Status.SUSPENDED);
        return tenant;
    }

    @Override
    public Tenant activateTenant(Long tenantId) {
        Tenant tenant = getTenantOrThrow(tenantId);

        if (tenant.getStatus() == Status.DELETED) {
            throw new IllegalStateException("Deleted tenant cannot be activated.");
        }

        tenant.changeStatus(Status.ACTIVE);
        return tenant;
    }

    @Override
    public Tenant deleteTenant(Long tenantId) {
        Tenant tenant = getTenantOrThrow(tenantId);

        if (tenant.getStatus() == Status.DELETED) {
            throw new IllegalStateException("Tenant already deleted.");
        }

        tenant.changeStatus(Status.DELETED);
        return tenant;
    }

    @Override
    @Transactional(readOnly = true)
    public Tenant getTenant(Long tenantId) {
        return getTenantOrThrow(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    private Tenant getTenantOrThrow(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found."));
    }

}
