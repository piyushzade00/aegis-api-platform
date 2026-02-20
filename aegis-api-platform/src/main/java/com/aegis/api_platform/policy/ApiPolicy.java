package com.aegis.api_platform.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiPolicy {

    private final Integer rateLimitPerMinute;
    private final Long monthlyQuota;
}
