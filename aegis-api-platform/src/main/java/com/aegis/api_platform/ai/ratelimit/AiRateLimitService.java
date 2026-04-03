package com.aegis.api_platform.ai.ratelimit;

import com.aegis.api_platform.exception.AiRateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRateLimitService {

    private final StringRedisTemplate redisTemplate;

    @Value("${ai.rag.daily-limit:50}")
    private int dailyLimit;

    private static final String RATE_LIMIT_SCRIPT = """
        local current = redis.call('INCR', KEYS[1])
        if current == 1 then
            redis.call('EXPIRE', KEYS[1], 86400)
        end
        return current
        """;

    public void checkAiQueryLimit(Long tenantId) {

        String key = buildKey(tenantId);

        RedisScript<Long> script = RedisScript.of(RATE_LIMIT_SCRIPT, Long.class);

        Long currentCount = redisTemplate.execute(script, List.of(key));

        if (currentCount != null && currentCount > dailyLimit) {
            log.warn("AI query limit exceeded for tenant {}", tenantId);
            throw new AiRateLimitExceededException(
                    "Daily AI query limit of " + dailyLimit +
                            " exceeded. Resets at midnight UTC."
            );
        }
    }

    public Long getRemainingQueries(Long tenantId) {
        String key = buildKey(tenantId);
        String value = redisTemplate.opsForValue().get(key);
        long used = value != null ? Long.parseLong(value) : 0L;
        return Math.max(0, dailyLimit - used);
    }

    private String buildKey(Long tenantId) {
        String date = LocalDate.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ai:rag:" + tenantId + ":" + date;
    }
}
