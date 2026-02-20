package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.exception.MonthlyQuotaExceededException;
import com.aegis.api_platform.service.QuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void checkMonthlyQuota(Long tenantId,
                                  Long apiId,
                                  Long allowedMonthlyQuota) {

        String key = buildKey(tenantId);

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount == 1) {
            redisTemplate.expire(key, Duration.ofDays(31));
        }

        if (currentCount != null && currentCount > allowedMonthlyQuota) {
            throw new MonthlyQuotaExceededException("Monthly quota exceeded");
        }
    }

    private String buildKey(Long tenantId) {
        String month = YearMonth.now()
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        return "quota:" + tenantId + ":" + month;
    }
}

