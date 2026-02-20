package com.aegis.api_platform.analytics.projection;

import java.time.LocalDate;

public interface DailyUsageProjection {

    LocalDate getDate();
    Long getTotalRequests();
}
