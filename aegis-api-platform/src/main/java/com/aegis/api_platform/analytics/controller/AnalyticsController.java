package com.aegis.api_platform.analytics.controller;

import com.aegis.api_platform.analytics.service.AnalyticsService;
import com.aegis.api_platform.dto.response.ApiUsageResponse;
import com.aegis.api_platform.dto.response.DailyUsageResponse;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;
import com.aegis.api_platform.dto.response.TenantUsageResponse;
import com.aegis.api_platform.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityUtils securityUtils;

    //System Admin APIs - can view usage for any tenant
    @GetMapping("/tenant/{tenantId}/total")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public TenantUsageResponse getTenantUsage(
            @PathVariable Long tenantId) {

        Long total =
                analyticsService.getTotalUsageForTenant(tenantId);

        return new TenantUsageResponse(tenantId, total);
    }

    @GetMapping("/tenant/{tenantId}/api-usage")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<ApiUsageResponse> getUsagePerApi(
            @PathVariable Long tenantId) {

        return analyticsService.getUsagePerApi(tenantId);
    }

    @GetMapping("/tenant/{tenantId}/daily")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<DailyUsageResponse> getDailyUsage(
            @PathVariable Long tenantId) {

        return analyticsService.getDailyUsage(tenantId);
    }

    @GetMapping("/tenant/{tenantId}/quota")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public QuotaAnalyticsResponse getQuota(
            @PathVariable Long tenantId) {

        return analyticsService.getQuotaStatus(tenantId);
    }

    //Tenant Admin APIs - can view their own usage
    @GetMapping("/me/total")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public TenantUsageResponse getMyTotalUsage() {

        Long tenantId = securityUtils.getCurrentTenantId();

        Long total = analyticsService.getTotalUsageForTenant(tenantId);

        return new TenantUsageResponse(tenantId, total);
    }

    @GetMapping("/me/api-usage")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public List<ApiUsageResponse> getMyApiUsage() {

        Long tenantId = securityUtils.getCurrentTenantId();

        return analyticsService.getUsagePerApi(tenantId);
    }

    @GetMapping("/me/daily")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public List<DailyUsageResponse> getMyDailyUsage() {

        Long tenantId = securityUtils.getCurrentTenantId();

        return analyticsService.getDailyUsage(tenantId);
    }

    @GetMapping("/me/quota")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public QuotaAnalyticsResponse getMyQuota() {

        Long tenantId = securityUtils.getCurrentTenantId();

        return analyticsService.getQuotaStatus(tenantId);
    }
}
