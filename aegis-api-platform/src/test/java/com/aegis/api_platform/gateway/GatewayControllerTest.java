package com.aegis.api_platform.gateway;

import com.aegis.api_platform.exception.MonthlyQuotaExceededException;
import com.aegis.api_platform.exception.RateLimitExceededException;
import com.aegis.api_platform.messaging.event.UsageEvent;
import com.aegis.api_platform.messaging.publisher.UsageEventPublisher;
import com.aegis.api_platform.metrics.GatewayMetrics;
import com.aegis.api_platform.model.ApiDefinition;
import com.aegis.api_platform.model.SubscriptionPlan;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.enums.Status;
import com.aegis.api_platform.policy.ApiPolicy;
import com.aegis.api_platform.service.ApiDefinitionService;
import com.aegis.api_platform.service.PolicyResolverService;
import com.aegis.api_platform.service.QuotaService;
import com.aegis.api_platform.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // setUp stubs not used by all tests
class GatewayControllerTest {

    @Mock
    private ApiDefinitionService apiDefinitionService;

    @Mock
    private WebClient webClient;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private QuotaService quotaService;

    @Mock
    private UsageEventPublisher usageEventPublisher;

    @Mock
    private PolicyResolverService policyResolverService;

    @Mock
    private BackendCallerService backendCallerService;

    @Mock
    private GatewayMetrics gatewayMetrics;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ApiDefinition apiDefinition;

    @Mock
    private Tenant tenant;

    @Mock
    private SubscriptionPlan subscriptionPlan;

    @InjectMocks
    private GatewayController gatewayController;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/gateway/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute("tenantId")).thenReturn(1L);
        when(request.getAttribute("apiKeyId")).thenReturn(100L);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        when(apiDefinition.getTenant()).thenReturn(tenant);
        when(apiDefinition.getTargetUrl()).thenReturn("http://backend-service/orders");
        when(apiDefinition.getId()).thenReturn(10L);
        when(tenant.getSubscriptionPlan()).thenReturn(subscriptionPlan);
        when(subscriptionPlan.getId()).thenReturn(1L);

        when(apiDefinitionService.resolveApi(anyLong(), anyString(), anyString()))
                .thenReturn(apiDefinition);

        when(policyResolverService.resolve(anyLong(), anyLong()))
                .thenReturn(new ApiPolicy(100, 100_000L));
    }

    @Test
    void handleGateway_shouldReturnResponse_whenRequestIsValid() {
        // Arrange - mock the WebClient fluent chain correctly
        WebClient.RequestBodyUriSpec requestBodyUriSpec =
                mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec =
                mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec =
                mock(WebClient.ResponseSpec.class);

        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(backendCallerService.call(any())).thenReturn(ResponseEntity.ok("success"));

        // Act
        ResponseEntity<String> response =
                gatewayController.handleGateway(request, null);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("success");

        verify(gatewayMetrics, times(1)).incrementGatewayRequest();
        verify(rateLimitService, times(1)).checkRateLimit(anyLong(), anyLong(), anyInt());
        verify(quotaService, times(1)).checkMonthlyQuota(anyLong(), anyLong(), anyLong());
        verify(usageEventPublisher, times(1)).publish(any(UsageEvent.class));
    }

    @Test
    void handleGateway_shouldThrow_whenRateLimitExceeded() {
        // Arrange
        doThrow(new RateLimitExceededException("Rate limit exceeded"))
                .when(rateLimitService)
                .checkRateLimit(anyLong(), anyLong(), anyInt());

        // Act + Assert
        assertThatThrownBy(() ->
                gatewayController.handleGateway(request, null))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Rate limit exceeded");

        verify(quotaService, never()).checkMonthlyQuota(anyLong(), anyLong(), anyLong());
        verify(usageEventPublisher, never()).publish(any());
    }

    @Test
    void handleGateway_shouldThrow_whenQuotaExceeded() {
        // Arrange
        doThrow(new MonthlyQuotaExceededException("Monthly quota exceeded"))
                .when(quotaService)
                .checkMonthlyQuota(anyLong(), anyLong(), anyLong());

        // Act + Assert
        assertThatThrownBy(() ->
                gatewayController.handleGateway(request, null))
                .isInstanceOf(MonthlyQuotaExceededException.class)
                .hasMessage("Monthly quota exceeded");

        verify(rateLimitService, times(1)).checkRateLimit(anyLong(), anyLong(), anyInt());
        verify(usageEventPublisher, never()).publish(any());
    }

    @Test
    void handleGateway_shouldThrow_whenApiNotFound() {
        // Arrange
        when(apiDefinitionService.resolveApi(anyLong(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("API not found"));

        // Act + Assert
        assertThatThrownBy(() ->
                gatewayController.handleGateway(request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("API not found");

        verify(rateLimitService, never()).checkRateLimit(anyLong(), anyLong(), anyInt());
        verify(quotaService, never()).checkMonthlyQuota(anyLong(), anyLong(), anyLong());
        verify(usageEventPublisher, never()).publish(any());
    }
}