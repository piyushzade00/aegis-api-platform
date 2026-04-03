package com.aegis.api_platform.ai.mcp;

import com.aegis.api_platform.analytics.service.AnalyticsService;
import com.aegis.api_platform.dto.response.ApiUsageResponse;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;
import com.aegis.api_platform.dto.response.TenantResponse;
import com.aegis.api_platform.dto.response.TenantUsageResponse;
import com.aegis.api_platform.mapper.TenantMapper;
import com.aegis.api_platform.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AegisMcpTools {

    private final TenantService tenantService;
    private final AnalyticsService analyticsService;
    private final TenantMapper tenantMapper;

    @Tool(description = """
        Get total API usage for a specific tenant.
        Returns total request count for the last 30 days.
        Use this when asked about tenant activity or usage volume.
        """)
    public TenantUsageResponse getTenantUsage(
            @ToolParam(description = "The numeric tenant ID") Long tenantId) {
        Long total =
                 analyticsService.getTotalUsageForTenant(tenantId);
        return new TenantUsageResponse(tenantId, total);
    }

    @Tool(description = """
        Get monthly quota status for a tenant.
        Returns quota limit, amount used, and remaining quota.
        Use this when asked about quota, limits, or remaining capacity.
        """)
    public QuotaAnalyticsResponse getQuotaStatus(
            @ToolParam(description = "The numeric tenant ID") Long tenantId) {
        return analyticsService.getQuotaStatus(tenantId);
    }

    @Tool(description = """
        Suspend a tenant. Use only when explicitly instructed.
        This blocks all gateway requests for the tenant immediately.
        """)
    public TenantResponse suspendTenant(
            @ToolParam(description = "The numeric tenant ID") Long tenantId) {
        return tenantMapper.toResponse(tenantService.suspendTenant(tenantId));
    }

    @Tool(description = """
        List all tenants with their current status.
        Use this when asked for an overview of all tenants.
        """)
    public List<TenantResponse> listAllTenants() {
        return tenantService.getAllTenants()
                .stream()
                .map(tenantMapper::toResponse)
                .toList();
    }

    @Tool(description = """
        Get per-API usage breakdown for a tenant.
        Returns which APIs are called most.
        Use this when asked about API-level traffic patterns.
        """)
    public List<ApiUsageResponse> getApiBreakdown(
            @ToolParam(description = "The numeric tenant ID") Long tenantId) {
        Pageable pageable = PageRequest.of(0, 10);
        return analyticsService.getUsagePerApi(tenantId, pageable)
                .getContent();
    }
}
