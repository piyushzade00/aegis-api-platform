package com.aegis.api_platform.repository;

import com.aegis.api_platform.model.AlertLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {
}
