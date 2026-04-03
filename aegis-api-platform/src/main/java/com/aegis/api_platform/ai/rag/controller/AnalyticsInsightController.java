package com.aegis.api_platform.ai.rag.controller;

import com.aegis.api_platform.ai.rag.service.UsageInsightService;
import com.aegis.api_platform.dto.request.AiInsightRequest;
import com.aegis.api_platform.dto.response.AiInsightResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/me/insights")
@RequiredArgsConstructor
public class AnalyticsInsightController {

    private final UsageInsightService insightService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public AiInsightResponse getInsight(
            @Valid @RequestBody AiInsightRequest request) {
        return insightService.getInsight(request);
    }
}
