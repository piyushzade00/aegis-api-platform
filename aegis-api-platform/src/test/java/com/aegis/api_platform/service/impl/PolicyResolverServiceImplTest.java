package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.model.PlanApiConfig;
import com.aegis.api_platform.model.SubscriptionPlan;
import com.aegis.api_platform.policy.ApiPolicy;
import com.aegis.api_platform.repository.SubscriptionPlanRepository;
import com.aegis.api_platform.service.PlanApiConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PolicyResolverServiceImplTest {

    @Mock
    private SubscriptionPlanRepository planRepository;

    @Mock
    private PlanApiConfigService planApiConfigService;

    @Mock
    private PlanApiConfig config;

    @InjectMocks
    private PolicyResolverServiceImpl policyResolverService;

    private SubscriptionPlan plan;

    @BeforeEach
    void setUp() {
        // This runs before every test. Creates a plan with known defaults.
        plan = new SubscriptionPlan(
                "PRO",
                "Pro Plan",
                100_000L,   // monthlyQuota
                100,         // rateLimitPerMinute
                new BigDecimal("49.99"),
                "USD"
        );
    }

    @Test
    void resolve_shouldReturnPlanDefaults_whenNoOverrideExists() {
        // Arrange
        Long planId = 1L;
        Long apiId = 10L;

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planApiConfigService.getConfig(planId, apiId)).thenReturn(Optional.empty());

        // Act
        ApiPolicy policy = policyResolverService.resolve(planId, apiId);

        // Assert
        assertThat(policy.getRateLimitPerMinute()).isEqualTo(100);
        assertThat(policy.getMonthlyQuota()).isEqualTo(100_000L);
    }

    @Test
    void resolve_shouldReturnOverrideValues_whenOverrideExists() {
        // Arrange
        Long planId = 1L;
        Long apiId = 10L;

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(config.getRateLimitPerMinuteOverride()).thenReturn(10);
        when(config.getMonthlyQuotaOverride()).thenReturn(500L);
        when(planApiConfigService.getConfig(planId, apiId)).thenReturn(Optional.of(config));

        // Act
        ApiPolicy policy = policyResolverService.resolve(planId, apiId);

        // Assert
        assertThat(policy.getRateLimitPerMinute()).isEqualTo(10);
        assertThat(policy.getMonthlyQuota()).isEqualTo(500L);
    }

    @Test
    void resolve_shouldUseOverrideRateOnly_whenOnlyRateOverrideSet() {
        // Arrange
        Long planId = 1L;
        Long apiId = 10L;

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(config.getRateLimitPerMinuteOverride()).thenReturn(5);
        when(config.getMonthlyQuotaOverride()).thenReturn(null);  // no quota override
        when(planApiConfigService.getConfig(planId, apiId)).thenReturn(Optional.of(config));

        // Act
        ApiPolicy policy = policyResolverService.resolve(planId, apiId);

        // Assert
        assertThat(policy.getRateLimitPerMinute()).isEqualTo(5);           // override applied
        assertThat(policy.getMonthlyQuota()).isEqualTo(100_000L);          // plan default used
    }

    @Test
    void resolve_shouldThrow_whenPlanNotFound() {
        // Arrange
        Long planId = 99L;
        Long apiId = 10L;

        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> policyResolverService.resolve(planId, apiId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Plan not found");
    }
}
