package com.aegis.api_platform.dto.response;

public record AiInsightResponse(
        String answer,
        Long remainingDailyQueries
) {}
