package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.exception.RateLimitExceededException;
import com.aegis.api_platform.metrics.GatewayMetrics;
import com.aegis.api_platform.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final GatewayMetrics gatewayMetrics;

    private static final String RATE_LIMIT_SCRIPT = """
        local current = redis.call('INCR', KEYS[1])
        if current == 1 then
            redis.call('EXPIRE', KEYS[1], 60)
        end
        return current
        """;

    @Override
    public void checkRateLimit(Long tenantId,
                               Long apiId,
                               Integer allowedPerMinute) {

        String key = buildKey(tenantId, apiId);

        RedisScript<Long> script = RedisScript.of(RATE_LIMIT_SCRIPT, Long.class);

        Long currentCount = redisTemplate.execute(script, List.of(key));

        if (currentCount != null && currentCount > allowedPerMinute) {
            gatewayMetrics.incrementRateLimitExceeded();    // Increment rate limit exceeded metric
            throw new RateLimitExceededException("Rate limit exceeded");
        }
    }

    private String buildKey(Long tenantId, Long apiId) {
        String minute = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        return "rate:" + tenantId + ":" + apiId + ":" + minute;
    }
}
