package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.exception.MonthlyQuotaExceededException;
import com.aegis.api_platform.repository.UsageLogRepository;
import com.aegis.api_platform.service.QuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {

    private final StringRedisTemplate redisTemplate;
    private final UsageLogRepository usageLogRepository;

    @Override
    public void checkMonthlyQuota(Long tenantId,
                                  Long apiId,
                                  Long allowedMonthlyQuota) {

        String key = buildKey(tenantId);

        Boolean exists = redisTemplate.hasKey(key);

        if (!exists) {
            YearMonth currentMonth = YearMonth.now();

            LocalDateTime start =
                    currentMonth.atDay(1).atStartOfDay();

            LocalDateTime end =
                    currentMonth.plusMonths(1)
                            .atDay(1)
                            .atStartOfDay();

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
            throw new MonthlyQuotaExceededException("Monthly quota exceeded");
        }
    }

    private String buildKey(Long tenantId) {
        String month = YearMonth.now()
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        return "quota:" + tenantId + ":" + month;
    }
}

