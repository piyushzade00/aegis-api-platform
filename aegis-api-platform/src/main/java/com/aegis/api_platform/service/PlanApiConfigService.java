package com.aegis.api_platform.service;

import com.aegis.api_platform.model.PlanApiConfig;

import java.util.Optional;

public interface PlanApiConfigService {

    Optional<PlanApiConfig> getConfig(
            Long planId,
            Long apiId
    );
}
