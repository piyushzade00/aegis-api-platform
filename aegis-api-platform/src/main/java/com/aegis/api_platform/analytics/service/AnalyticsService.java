package com.aegis.api_platform.analytics.service;

import com.aegis.api_platform.dto.response.ApiUsageResponse;
import com.aegis.api_platform.dto.response.DailyUsageResponse;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface AnalyticsService {

    Long getTotalUsageForTenant(Long tenantId);

    Long getTotalUsageForTenant(Long tenantId, Instant start, Instant end);

    Page<ApiUsageResponse> getUsagePerApi(Long tenantId, Pageable pageable);

    Page<ApiUsageResponse> getUsagePerApi(Long tenantId,
                                          Instant start,
                                          Instant end,
                                          Pageable pageable);

    List<DailyUsageResponse> getDailyUsage(Long tenantId);

    List<DailyUsageResponse> getDailyUsage(Long tenantId, Instant start, Instant end);

    QuotaAnalyticsResponse getQuotaStatus(Long tenantId);
}
