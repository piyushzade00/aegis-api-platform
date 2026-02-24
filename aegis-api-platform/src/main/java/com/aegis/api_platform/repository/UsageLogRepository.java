package com.aegis.api_platform.repository;

import com.aegis.api_platform.analytics.projection.ApiUsageProjection;
import com.aegis.api_platform.analytics.projection.DailyUsageProjection;
import com.aegis.api_platform.model.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {

    @Query("""
       SELECT COUNT(u)
       FROM UsageLog u
       WHERE u.tenantId = :tenantId
       """)
    Long countTotalByTenant(@Param("tenantId") Long tenantId);

    @Query("""
       SELECT u.apiId AS apiId,
              COUNT(u) AS totalRequests
       FROM UsageLog u
       WHERE u.tenantId = :tenantId
       GROUP BY u.apiId
       """)
    List<ApiUsageProjection> countUsagePerApi(
            @Param("tenantId") Long tenantId
    );

    @Query("""
       SELECT DATE(u.createdAt) AS date,
              COUNT(u) AS totalRequests
       FROM UsageLog u
       WHERE u.tenantId = :tenantId
       GROUP BY DATE(u.createdAt)
       ORDER BY DATE(u.createdAt)
       """)
    List<DailyUsageProjection> countDailyUsage(
            @Param("tenantId") Long tenantId
    );

    @Query("""
   SELECT COUNT(u)
   FROM UsageLog u
   WHERE u.tenantId = :tenantId
   AND u.createdAt >= :start
   AND u.createdAt < :end
   """)
    Long countMonthlyUsage(
            @Param("tenantId") Long tenantId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
