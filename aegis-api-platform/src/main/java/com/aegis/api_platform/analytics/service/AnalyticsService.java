package com.aegis.api_platform.analytics.service;

import com.aegis.api_platform.dto.response.ApiUsageResponse;
import com.aegis.api_platform.dto.response.DailyUsageResponse;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;

import java.time.Instant;
import java.util.List;

public interface AnalyticsService {

    Long getTotalUsageForTenant(Long tenantId);

    Long getTotalUsageForTenant(Long tenantId, Instant start, Instant end);

    List<ApiUsageResponse> getUsagePerApi(Long tenantId);

    List<ApiUsageResponse> getUsagePerApi(Long tenantId, Instant start, Instant end);

    List<DailyUsageResponse> getDailyUsage(Long tenantId);

    List<DailyUsageResponse> getDailyUsage(Long tenantId, Instant start, Instant end);

    QuotaAnalyticsResponse getQuotaStatus(Long tenantId);
}
