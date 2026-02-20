package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.model.PlanApiConfig;
import com.aegis.api_platform.repository.PlanApiConfigRepository;
import com.aegis.api_platform.service.PlanApiConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanApiConfigServiceImpl
        implements PlanApiConfigService {

    private final PlanApiConfigRepository repository;

    @Override
    public Optional<PlanApiConfig> getConfig(
            Long planId,
            Long apiId
    ) {
        return repository.findByPlanIdAndApiId(
                planId,
                apiId
        );
    }
}
