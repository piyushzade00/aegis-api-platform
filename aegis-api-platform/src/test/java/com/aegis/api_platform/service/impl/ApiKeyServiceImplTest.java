package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.enums.ApiKeyStatus;
import com.aegis.api_platform.enums.Status;
import com.aegis.api_platform.model.ApiKey;
import com.aegis.api_platform.model.SubscriptionPlan;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.repository.ApiKeyRepository;
import com.aegis.api_platform.repository.TenantRepository;
import com.aegis.api_platform.util.ApiKeyHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyHasher apiKeyHasher;

    @InjectMocks
    private ApiKeyServiceImpl apiKeyService;

    private Tenant activeTenant;
    private SubscriptionPlan activePlan;

    @BeforeEach
    void setUp() {
        activePlan = new SubscriptionPlan(
                "PRO", "Pro Plan", 100_000L, 100,
                new BigDecimal("49.99"), "USD"
        );

        activeTenant = new Tenant("Acme Corp", Status.ACTIVE, activePlan);
    }

    // -------- createKey --------

    @Test
    void createKey_shouldReturnRawKey_whenTenantIsActive() {
        // Arrange
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant));
        when(apiKeyHasher.hash(anyString())).thenReturn("hashed_value");
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        String rawKey = apiKeyService.createKey(1L, null);

        // Assert
        assertThat(rawKey).startsWith("ak_live_");
        verify(apiKeyRepository, times(1)).save(any(ApiKey.class));
    }

    @Test
    void createKey_shouldThrow_whenTenantNotFound() {
        // Arrange
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> apiKeyService.createKey(99L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant not found");
    }

    @Test
    void createKey_shouldThrow_whenTenantIsInactive() {
        // Arrange
        Tenant suspendedTenant = new Tenant("Suspended Corp", Status.SUSPENDED, activePlan);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(suspendedTenant));

        // Act + Assert
        assertThatThrownBy(() -> apiKeyService.createKey(1L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot create API key for inactive tenant");
    }

    // -------- validateRawKey --------

    @Test
    void validateRawKey_shouldReturnApiKey_whenKeyIsValid() {
        // Arrange
        ApiKey apiKey = new ApiKey(activeTenant, "hashed_value", null);

        when(apiKeyHasher.hash("raw_key")).thenReturn("hashed_value");
        when(apiKeyRepository.findByHashedKeyAndStatus("hashed_value", ApiKeyStatus.ACTIVE))
                .thenReturn(Optional.of(apiKey));

        // Act
        ApiKey result = apiKeyService.validateRawKey("raw_key");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTenant().getName()).isEqualTo("Acme Corp");
    }

    @Test
    void validateRawKey_shouldThrow_whenKeyNotFound() {
        // Arrange
        when(apiKeyHasher.hash(anyString())).thenReturn("hashed_value");
        when(apiKeyRepository.findByHashedKeyAndStatus(anyString(), any()))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> apiKeyService.validateRawKey("invalid_key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid API key");
    }

    @Test
    void validateRawKey_shouldThrow_whenKeyIsExpired() {
        // Arrange - key expired yesterday
        ApiKey expiredKey = new ApiKey(
                activeTenant,
                "hashed_value",
                Instant.now().minusSeconds(86400)  // expired 24 hours ago
        );

        when(apiKeyHasher.hash(anyString())).thenReturn("hashed_value");
        when(apiKeyRepository.findByHashedKeyAndStatus(anyString(), any()))
                .thenReturn(Optional.of(expiredKey));

        // Act + Assert
        assertThatThrownBy(() -> apiKeyService.validateRawKey("raw_key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("API key expired");
    }

    @Test
    void validateRawKey_shouldThrow_whenTenantIsInactive() {
        // Arrange - valid key but tenant is suspended
        Tenant suspendedTenant = new Tenant("Suspended Corp", Status.SUSPENDED, activePlan);
        ApiKey apiKey = new ApiKey(suspendedTenant, "hashed_value", null);

        when(apiKeyHasher.hash(anyString())).thenReturn("hashed_value");
        when(apiKeyRepository.findByHashedKeyAndStatus(anyString(), any()))
                .thenReturn(Optional.of(apiKey));

        // Act + Assert
        assertThatThrownBy(() -> apiKeyService.validateRawKey("raw_key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Tenant inactive");
    }

    // -------- revokeKey --------

    @Test
    void revokeKey_shouldRevoke_whenKeyExists() {
        // Arrange
        ApiKey apiKey = new ApiKey(activeTenant, "hashed_value", null);
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(apiKey));

        // Act
        apiKeyService.revokeKey(1L);

        // Assert - status should now be REVOKED
        assertThat(apiKey.getStatus()).isEqualTo(ApiKeyStatus.REVOKED);
    }

    @Test
    void revokeKey_shouldThrow_whenKeyNotFound() {
        // Arrange
        when(apiKeyRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> apiKeyService.revokeKey(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("API key not found");
    }
}