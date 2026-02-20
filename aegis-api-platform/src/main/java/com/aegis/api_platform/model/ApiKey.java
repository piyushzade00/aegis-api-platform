package com.aegis.api_platform.model;

import com.aegis.api_platform.enums.ApiKeyStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "api_key",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "hashed_key")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "hashed_key", nullable = false, length = 128)
    private String hashedKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiKeyStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public ApiKey(Tenant tenant,
                  String hashedKey,
                  Instant expiresAt) {

        if (tenant == null)
            throw new IllegalArgumentException("Tenant required");

        if (hashedKey == null || hashedKey.isBlank())
            throw new IllegalArgumentException("Hashed key required");

        this.tenant = tenant;
        this.hashedKey = hashedKey;
        this.status = ApiKeyStatus.ACTIVE;
        this.expiresAt = expiresAt;
    }

    public void revoke() {
        this.status = ApiKeyStatus.REVOKED;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
