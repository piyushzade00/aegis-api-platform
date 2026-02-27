package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.exception.MonthlyQuotaExceededException;
import com.aegis.api_platform.metrics.GatewayMetrics;
import com.aegis.api_platform.repository.UsageLogRepository;
import com.aegis.api_platform.service.QuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {

    private final StringRedisTemplate redisTemplate;
    private final UsageLogRepository usageLogRepository;
    private final GatewayMetrics gatewayMetrics;

    @Override
    public void checkMonthlyQuota(Long tenantId,
                                  Long apiId,
                                  Long allowedMonthlyQuota) {

        String key = buildKey(tenantId);

        Boolean exists = redisTemplate.hasKey(key);

        if (!exists) {
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

            Long countFromDb =
                    usageLogRepository.countMonthlyUsage(
                            tenantId,
                            start,
                            end
                    );

            if (countFromDb == null) {
                countFromDb = 0L;
            }

            redisTemplate.opsForValue().set(
                    key,
                    String.valueOf(countFromDb),
                    Duration.ofDays(31)
            );
        }

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount != null && currentCount > allowedMonthlyQuota) {
            gatewayMetrics.incrementMonthlyQuotaExceeded(); // Increment the metric counter for quota exceedance
            throw new MonthlyQuotaExceededException("Monthly quota exceeded");
        }
    }

    private String buildKey(Long tenantId) {
        String month = YearMonth.now()
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        return "quota:" + tenantId + ":" + month;
    }
}

