package com.aegis.api_platform.repository;

import com.aegis.api_platform.model.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    boolean existsByPlanCodeIgnoreCase(@NotBlank String planCode);
}
