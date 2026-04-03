package com.aegis.api_platform.repository;

import com.aegis.api_platform.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySubscriptionPlanId(Long subscriptionPlanId);

    @Query("SELECT t.id FROM Tenant t WHERE t.status = 'ACTIVE'")
    List<Long> findActiveTenantIds();

    @Query("""
            SELECT sp.monthlyQuota 
            FROM Tenant t 
            JOIN t.subscriptionPlan sp 
            WHERE t.id = :tenantId
            """)
    Long findMonthlyQuotaByTenantId(Long tenantId);
}
