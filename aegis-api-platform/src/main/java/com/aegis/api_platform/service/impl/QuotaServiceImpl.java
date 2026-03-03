package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.exception.MonthlyQuotaExceededException;
import com.aegis.api_platform.metrics.GatewayMetrics;
import com.aegis.api_platform.repository.UsageLogRepository;
import com.aegis.api_platform.service.QuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {

    private final StringRedisTemplate redisTemplate;
    private final UsageLogRepository usageLogRepository;
    private final GatewayMetrics gatewayMetrics;

    private static final String QUOTA_SCRIPT = """
        local current = redis.call('GET', KEYS[1])
        if current == false then
            redis.call('SET', KEYS[1], ARGV[1])
            redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
            current = redis.call('INCR', KEYS[1])
        else
            current = redis.call('INCR', KEYS[1])
        end
        return current
        """;

    @Override
    public void checkMonthlyQuota(Long tenantId,
                                  Long apiId,
                                  Long allowedMonthlyQuota) {

        String key = buildKey(tenantId);

        // Seed value from DB (this query is outside the script — acceptable,
        // the script handles the race on SET)
        Long seedValue = getSeedValue(tenantId);

        long ttlSeconds = getRemainingSecondsInMonth();

        RedisScript<Long> script = RedisScript.of(QUOTA_SCRIPT, Long.class);

        Long currentCount = redisTemplate.execute(
                script,
                List.of(key),
                String.valueOf(seedValue),
                String.valueOf(ttlSeconds)
        );

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

    private Long getSeedValue(Long tenantId) {
        YearMonth currentMonth = YearMonth.now();
        Instant start = currentMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = currentMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Long count = usageLogRepository.countMonthlyUsage(tenantId, start, end);
        return count != null ? count : 0L;
    }

    private long getRemainingSecondsInMonth() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime endOfMonth = now.with(now.getMonth().maxLength() == now.getDayOfMonth()
                        ? java.time.temporal.TemporalAdjusters.firstDayOfNextMonth()
                        : java.time.temporal.TemporalAdjusters.firstDayOfNextMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        return java.time.Duration.between(now, endOfMonth).getSeconds();
    }
}

