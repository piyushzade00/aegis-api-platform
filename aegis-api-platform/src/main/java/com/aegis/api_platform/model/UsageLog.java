package com.aegis.api_platform.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "usage_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "api_id", nullable = false)
    private Long apiId;

    @Column(name = "api_key_id", nullable = false)
    private Long apiKeyId;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "latency_ms", nullable = false)
    private long latencyMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UsageLog(String eventId,
                    Long tenantId,
                    Long apiId,
                    Long apiKeyId,
                    int statusCode,
                    long latencyMs) {

        this.eventId = eventId;
        this.tenantId = tenantId;
        this.apiId = apiId;
        this.apiKeyId = apiKeyId;
        this.statusCode = statusCode;
        this.latencyMs = latencyMs;
        this.createdAt = Instant.now();
    }
}
