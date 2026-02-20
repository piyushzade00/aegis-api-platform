package com.aegis.api_platform.analytics.service;

import com.aegis.api_platform.dto.response.ApiUsageResponse;
import com.aegis.api_platform.dto.response.DailyUsageResponse;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;

import java.util.List;

public interface AnalyticsService {

    Long getTotalUsageForTenant(Long tenantId);

    List<ApiUsageResponse> getUsagePerApi(Long tenantId);

    List<DailyUsageResponse> getDailyUsage(Long tenantId);

    QuotaAnalyticsResponse getQuotaStatus(Long tenantId);
}
