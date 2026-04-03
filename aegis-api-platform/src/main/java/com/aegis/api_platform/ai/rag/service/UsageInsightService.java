package com.aegis.api_platform.ai.rag.service;

import com.aegis.api_platform.ai.rag.guard.PromptGuardService;
import com.aegis.api_platform.ai.ratelimit.AiRateLimitService;
import com.aegis.api_platform.analytics.service.AnalyticsService;
import com.aegis.api_platform.dto.request.AiInsightRequest;
import com.aegis.api_platform.dto.response.AiInsightResponse;
import com.aegis.api_platform.dto.response.ApiUsageResponse;
import com.aegis.api_platform.dto.response.DailyUsageResponse;
import com.aegis.api_platform.dto.response.QuotaAnalyticsResponse;
import com.aegis.api_platform.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageInsightService {

    private final ChatClient chatClient;
    private final AnalyticsService analyticsService;
    private final PromptBuilderService promptBuilderService;
    private final PromptGuardService promptGuardService;
    private final AiRateLimitService aiRateLimitService;
    private final SecurityUtils securityUtils;

    public AiInsightResponse getInsight(AiInsightRequest request) {

        Long tenantId = securityUtils.getCurrentTenantId();

        // Step 1: Guard — validate input before anything else
        promptGuardService.validate(request.question());

        // Step 2: AI rate limit check
        aiRateLimitService.checkAiQueryLimit(tenantId);

        // Step 3: Fetch data using existing services
        Long totalRequests = analyticsService.getTotalUsageForTenant(tenantId);

        List<ApiUsageResponse> apiBreakdown = analyticsService
                .getUsagePerApi(tenantId, PageRequest.of(0, 10))
                .getContent();

        List<DailyUsageResponse> dailyUsage =
                analyticsService.getDailyUsage(tenantId);

        QuotaAnalyticsResponse quota =
                analyticsService.getQuotaStatus(tenantId);

        // Step 4: Build prompts
        String systemPrompt = promptBuilderService.buildSystemPrompt();

        String userContext = promptBuilderService.buildUserContext(
                tenantId,
                request.question(),
                totalRequests,
                apiBreakdown,
                dailyUsage,
                quota
        );

        // Step 5: Call LLM
        log.info("Sending RAG query for tenant {}", tenantId);

        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(userContext)
                .call()
                .content();

        // Step 6: Get remaining queries for response
        Long remaining = aiRateLimitService.getRemainingQueries(tenantId);

        return new AiInsightResponse(answer, remaining);
    }
}
