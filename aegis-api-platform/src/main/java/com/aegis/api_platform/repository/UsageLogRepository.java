package com.aegis.api_platform.repository;

import com.aegis.api_platform.model.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {

    @Query("""
           SELECT COUNT(u)
           FROM UsageLog u
           WHERE u.tenantId = :tenantId
           AND FUNCTION('DATE_FORMAT', u.createdAt, '%Y%m') = :yearMonth
           """)
    Long countByTenantAndMonth(@Param("tenantId") Long tenantId,
                               @Param("yearMonth") String yearMonth);
}
