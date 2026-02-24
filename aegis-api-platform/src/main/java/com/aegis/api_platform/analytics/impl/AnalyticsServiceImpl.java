package com.aegis.api_platform.analytics.impl;

import com.aegis.api_platform.analytics.service.AnalyticsService;
import com.aegis.api_platform.dto.response.ApiUsageResponse;
import com.aegis.api_platform.dto.response.DailyUsageResponse;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.repository.TenantRepository;
import com.aegis.api_platform.repository.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UsageLogRepository usageLogRepository;
    private final TenantRepository tenantRepository;

    @Override
    public Long getTotalUsageForTenant(Long tenantId) {
        return usageLogRepository.countTotalByTenant(tenantId);
    }

    @Override
    public List<ApiUsageResponse> getUsagePerApi(Long tenantId) {

        return usageLogRepository.countUsagePerApi(tenantId)
                .stream()
                .map(p -> new ApiUsageResponse(
                        p.getApiId(),
                        p.getTotalRequests()
                ))
                .toList();
    }

    @Override
    public List<DailyUsageResponse> getDailyUsage(Long tenantId) {

        return usageLogRepository.countDailyUsage(tenantId)
                .stream()
                .map(p -> new DailyUsageResponse(
                        p.getDate(),
                        p.getTotalRequests()
                ))
                .toList();
    }

    @Override
    public QuotaAnalyticsResponse getQuotaStatus(Long tenantId) {

        YearMonth currentMonth = YearMonth.now();

        Instant start =
                currentMonth.atDay(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant();

        Instant end =
                currentMonth.plusMonths(1)
                        .atDay(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant();

        Long used =
                usageLogRepository.countMonthlyUsage(
                        tenantId,
                        start,
                        end
                );

        if (used == null) {
            used = 0L;
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        Long monthlyQuota =
                tenant.getSubscriptionPlan().getMonthlyQuota();

        Long remaining = Math.max(0, monthlyQuota - used);

        return new QuotaAnalyticsResponse(
                tenantId,
                monthlyQuota,
                used,
                remaining
        );
    }
}
