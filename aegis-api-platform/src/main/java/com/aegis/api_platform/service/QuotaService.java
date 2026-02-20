package com.aegis.api_platform.service;

public interface QuotaService {

    void checkMonthlyQuota(Long tenantId,
                           Long apiId,
                           Long allowedMonthlyQuota);
}
