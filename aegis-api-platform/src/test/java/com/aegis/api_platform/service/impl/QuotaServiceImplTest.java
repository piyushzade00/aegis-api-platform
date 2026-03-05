package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.exception.MonthlyQuotaExceededException;
import com.aegis.api_platform.metrics.GatewayMetrics;
import com.aegis.api_platform.repository.UsageLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotaServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private UsageLogRepository usageLogRepository;

    @Mock
    private GatewayMetrics gatewayMetrics;

    @InjectMocks
    private QuotaServiceImpl quotaService;

    @Test
    void checkMonthlyQuota_shouldPass_whenCountWithinQuota() {
        // Arrange
        // DB returns 50 existing requests this month
        when(usageLogRepository.countMonthlyUsage(
                anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(50L);

        // Redis Lua script returns 51 after increment (50 seed + 1)
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                .thenReturn(51L);

        // Act + Assert - quota is 1000, count is 51, should pass
        assertThatCode(() ->
                quotaService.checkMonthlyQuota(1L, 10L, 1000L))
                .doesNotThrowAnyException();

        verify(gatewayMetrics, never()).incrementMonthlyQuotaExceeded();
    }

    @Test
    void checkMonthlyQuota_shouldPass_whenCountEqualsQuota() {
        // Arrange
        when(usageLogRepository.countMonthlyUsage(
                anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(99L);

        // Exactly at the limit
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                .thenReturn(100L);

        // Act + Assert - quota is 100, count is 100, should still pass
        assertThatCode(() ->
                quotaService.checkMonthlyQuota(1L, 10L, 100L))
                .doesNotThrowAnyException();

        verify(gatewayMetrics, never()).incrementMonthlyQuotaExceeded();
    }

    @Test
    void checkMonthlyQuota_shouldThrow_whenCountExceedsQuota() {
        // Arrange
        when(usageLogRepository.countMonthlyUsage(
                anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(100L);

        // One over the limit
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                .thenReturn(101L);

        // Act + Assert - quota is 100, count is 101, should throw
        assertThatThrownBy(() ->
                quotaService.checkMonthlyQuota(1L, 10L, 100L))
                .isInstanceOf(MonthlyQuotaExceededException.class)
                .hasMessage("Monthly quota exceeded");

        verify(gatewayMetrics, times(1)).incrementMonthlyQuotaExceeded();
    }
}
