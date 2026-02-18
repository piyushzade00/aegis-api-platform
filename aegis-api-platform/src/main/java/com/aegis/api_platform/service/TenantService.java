package com.aegis.api_platform.service;

import com.aegis.api_platform.model.Tenant;

import java.util.List;

public interface TenantService {
    Tenant createTenant(String name);

    Tenant suspendTenant(Long tenantId);

    Tenant activateTenant(Long tenantId);

    Tenant deleteTenant(Long tenantId);

    Tenant getTenant(Long tenantId);

    List<Tenant> getAllTenants();
}
