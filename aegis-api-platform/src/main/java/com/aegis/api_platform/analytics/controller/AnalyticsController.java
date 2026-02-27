package com.aegis.api_platform.analytics.controller;

import com.aegis.api_platform.analytics.service.AnalyticsService;
import com.aegis.api_platform.dto.response.ApiUsageResponse;
import com.aegis.api_platform.dto.response.DailyUsageResponse;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;
import com.aegis.api_platform.dto.response.TenantUsageResponse;
import com.aegis.api_platform.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private record DateRange(Instant start, Instant end) {}

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
    public TenantUsageResponse getMyTotalUsage(@RequestParam(required = false) LocalDate from,
                                               @RequestParam(required = false) LocalDate to) {

        Long tenantId = securityUtils.getCurrentTenantId();

        DateRange range = resolveDateRange(from, to);

        Long total = analyticsService.getTotalUsageForTenant(
                tenantId,
                range.start(),
                range.end()
        );

        return new TenantUsageResponse(tenantId, total);
    }

    @GetMapping("/me/api-usage")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public List<ApiUsageResponse> getMyApiUsage(@RequestParam(required = false) LocalDate from,
                                                @RequestParam(required = false) LocalDate to) {

        Long tenantId = securityUtils.getCurrentTenantId();

        DateRange range = resolveDateRange(from, to);

        return analyticsService.getUsagePerApi(tenantId, range.start(),range.end());
    }

    @GetMapping("/me/daily")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public List<DailyUsageResponse> getMyDailyUsage(@RequestParam(required = false) LocalDate from,
                                                    @RequestParam(required = false) LocalDate to) {

        Long tenantId = securityUtils.getCurrentTenantId();

        DateRange range = resolveDateRange(from, to);

        return analyticsService.getDailyUsage(tenantId, range.start(),range.end());
    }

    @GetMapping("/me/quota")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public QuotaAnalyticsResponse getMyQuota() {

        Long tenantId = securityUtils.getCurrentTenantId();

        return analyticsService.getQuotaStatus(tenantId);
    }

    private DateRange resolveDateRange(LocalDate from, LocalDate to) {

        LocalDate today = LocalDate.now();

        if (from == null && to == null) {
            from = today.minusDays(30);
            to = today;
        } else if (from == null || to == null) {
            throw new IllegalArgumentException("Both from and to must be provided");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        if (ChronoUnit.DAYS.between(from, to) > 90) {
            throw new IllegalArgumentException("Maximum 90-day range allowed");
        }

        return new DateRange(
                from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        );
    }
}
