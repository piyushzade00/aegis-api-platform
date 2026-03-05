package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.exception.RateLimitExceededException;
import com.aegis.api_platform.metrics.GatewayMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private GatewayMetrics gatewayMetrics;

    @InjectMocks
    private RateLimitServiceImpl rateLimitService;

    @Test
    void checkRateLimit_shouldPass_whenCountWithinLimit() {
        // Arrange - simulate Redis returning count of 5, limit is 10
        when(redisTemplate.execute(any(RedisScript.class), anyList()))
                .thenReturn(5L);

        // Act + Assert - no exception should be thrown
        assertThatCode(() ->
                rateLimitService.checkRateLimit(1L, 10L, 10))
                .doesNotThrowAnyException();

        // Verify metrics were NOT incremented
        verify(gatewayMetrics, never()).incrementRateLimitExceeded();
    }

    @Test
    void checkRateLimit_shouldPass_whenCountEqualsLimit() {
        // Arrange - exactly at the limit, should still pass
        when(redisTemplate.execute(any(RedisScript.class), anyList()))
                .thenReturn(10L);

        // Act + Assert
        assertThatCode(() ->
                rateLimitService.checkRateLimit(1L, 10L, 10))
                .doesNotThrowAnyException();

        verify(gatewayMetrics, never()).incrementRateLimitExceeded();
    }

    @Test
    void checkRateLimit_shouldThrow_whenCountExceedsLimit() {
        // Arrange - simulate Redis returning count of 11, limit is 10
        when(redisTemplate.execute(any(RedisScript.class), anyList()))
                .thenReturn(11L);

        // Act + Assert
        assertThatThrownBy(() ->
                rateLimitService.checkRateLimit(1L, 10L, 10))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Rate limit exceeded");

        // Verify metric was incremented
        verify(gatewayMetrics, times(1)).incrementRateLimitExceeded();
    }
}
