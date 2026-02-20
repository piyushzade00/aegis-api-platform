package com.aegis.api_platform.service;

public interface RateLimitService {

    void checkRateLimit(Long tenantId,
                        Long apiId,
                        Integer allowedPerMinute);
}
