package com.aegis.api_platform.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "ai_alert_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlertLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id", unique = true)
    private String alertId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "severity_score")
    private double severityScore;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AlertLog(String alertId, Long tenantId,
                    String alertType, String message,
                    double severityScore) {
        this.alertId = alertId;
        this.tenantId = tenantId;
        this.alertType = alertType;
        this.message = message;
        this.severityScore = severityScore;
        this.createdAt = Instant.now();
    }
}
