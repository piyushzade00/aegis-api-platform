package com.aegis.api_platform.analytics.impl;

import com.aegis.api_platform.analytics.service.AnalyticsService;
import com.aegis.api_platform.dto.response.ApiUsageResponse;
import com.aegis.api_platform.dto.response.DailyUsageResponse;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.repository.TenantRepository;
import com.aegis.api_platform.repository.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UsageLogRepository usageLogRepository;
    private final TenantRepository tenantRepository;

    @Override
    public Long getTotalUsageForTenant(Long tenantId) {
        Instant now = Instant.now();
        Instant start = now.minus(30, ChronoUnit.DAYS);
        Instant end = now;

        return usageLogRepository.countTotalByTenant(tenantId, start, end);
    }

    @Override
    public Long getTotalUsageForTenant(Long tenantId, Instant start, Instant end) {
        return usageLogRepository.countTotalByTenant(tenantId, start, end);
    }

    @Override
    public Page<ApiUsageResponse> getUsagePerApi(Long tenantId, Pageable pageable) {
        Instant now = Instant.now();
        Instant start = now.minus(30, ChronoUnit.DAYS);
        Instant end = now;

        return usageLogRepository.countUsagePerApi(tenantId, start, end, pageable)
                .map(p -> new ApiUsageResponse(
                        p.getApiId(),
                        p.getTotalRequests()
                ));
    }

    @Override
    public Page<ApiUsageResponse> getUsagePerApi(Long tenantId,
                                                 Instant start,
                                                 Instant end,
                                                 Pageable pageable) {
        return usageLogRepository.countUsagePerApi(tenantId, start, end, pageable)
                .map(p -> new ApiUsageResponse(
                        p.getApiId(),
                        p.getTotalRequests()
                ));
    }

    @Override
    public List<DailyUsageResponse> getDailyUsage(Long tenantId) {
        Instant now = Instant.now();
        Instant start = now.minus(30, ChronoUnit.DAYS);
        Instant end = now;

        return usageLogRepository.countDailyUsage(tenantId, start, end)
                .stream()
                .map(p -> new DailyUsageResponse(
                        p.getDate(),
                        p.getTotalRequests()
                ))
                .toList();
    }

    @Override
    public List<DailyUsageResponse> getDailyUsage(Long tenantId, Instant start, Instant end) {
        return usageLogRepository.countDailyUsage(tenantId, start, end)
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
