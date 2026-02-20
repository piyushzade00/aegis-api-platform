package com.aegis.api_platform.dto.response;

import java.time.LocalDate;

public record DailyUsageResponse(
        LocalDate date,
        Long totalRequests
) {}
