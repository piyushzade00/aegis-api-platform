package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.exception.RateLimitExceededException;
import com.aegis.api_platform.metrics.GatewayMetrics;
import com.aegis.api_platform.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final GatewayMetrics gatewayMetrics;

    @Override
    public void checkRateLimit(Long tenantId,
                               Long apiId,
                               Integer allowedPerMinute) {

        String key = buildKey(tenantId, apiId);

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount == 1) {
            // first request in window
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        if (currentCount != null && currentCount > allowedPerMinute) {
            gatewayMetrics.incrementRateLimitExceeded();    // Increment rate limit exceeded metric
            throw new RateLimitExceededException("Rate limit exceeded");
        }
    }

    private String buildKey(Long tenantId, Long apiId) {
        String minute = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        return "rate:" + tenantId + ":" + apiId + ":" + minute;
    }
}
