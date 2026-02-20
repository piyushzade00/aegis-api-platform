package com.aegis.api_platform.repository;

import com.aegis.api_platform.model.PlanApiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanApiConfigRepository
        extends JpaRepository<PlanApiConfig, Long> {

    Optional<PlanApiConfig> findByPlanIdAndApiId(
            Long planId,
            Long apiId
    );
}
