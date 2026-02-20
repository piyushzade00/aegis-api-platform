package com.aegis.api_platform.gateway;

import com.aegis.api_platform.model.ApiDefinition;
import com.aegis.api_platform.service.ApiDefinitionService;
import com.aegis.api_platform.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class GatewayController {

    private final ApiDefinitionService apiDefinitionService;
    private final WebClient webClient;
    private final RateLimitService rateLimitService;

    @RequestMapping("/**")
    public ResponseEntity<String> handleGateway(HttpServletRequest request,
                                                @RequestBody(required = false) String body) {

        Long tenantId = (Long) request.getAttribute("tenantId");

        String fullPath = request.getRequestURI().replace("/gateway", "");
        String method = request.getMethod();

        // Remove /gateway prefix
        String path = fullPath.replaceFirst("/gateway", "");

        ApiDefinition api = apiDefinitionService.resolveApi(tenantId, path, method);

        // Check rate limit
        Integer allowedPerMinute =
                api.getTenant()
                        .getSubscriptionPlan()
                        .getRateLimitPerMinute();

        rateLimitService.checkRateLimit(
                tenantId,
                api.getId(),
                allowedPerMinute
        );

        //Forward request to target URL
        WebClient.RequestBodySpec requestSpec =
                webClient.method(HttpMethod.valueOf(method))
                        .uri(api.getTargetUrl());

        // Forward headers
        Collections.list(request.getHeaderNames())
                .forEach(header ->{
                    if (!header.equalsIgnoreCase("host")) {
                        requestSpec.header(header, request.getHeader(header));
                    }
                });

        WebClient.ResponseSpec responseSpec;

        if (body != null && !body.isBlank()) {
            responseSpec = requestSpec.bodyValue(body).retrieve();
        } else {
            responseSpec = requestSpec.retrieve();
        }

        return responseSpec.toEntity(String.class).block();
    }
}
