package com.aegis.api_platform.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "plan_api_config",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"plan_id", "api_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanApiConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiDefinition api;

    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinuteOverride;

    @Column(name = "monthly_quota_override")
    private Long monthlyQuotaOverride;

    public PlanApiConfig(
            SubscriptionPlan plan,
            ApiDefinition api,
            Integer rateLimitPerMinuteOverride,
            Long monthlyQuotaOverride
    ) {

        if (plan == null)
            throw new IllegalArgumentException("Plan required");

        if (api == null)
            throw new IllegalArgumentException("API required");

        if (rateLimitPerMinuteOverride != null &&
                rateLimitPerMinuteOverride <= 0)
            throw new IllegalArgumentException("Rate limit must be positive");

        if (monthlyQuotaOverride != null &&
                monthlyQuotaOverride <= 0)
            throw new IllegalArgumentException("Quota must be positive");

        this.plan = plan;
        this.api = api;
        this.rateLimitPerMinuteOverride = rateLimitPerMinuteOverride;
        this.monthlyQuotaOverride = monthlyQuotaOverride;
    }

    public void setRateLimitPerMinuteOverride(
            Integer rateLimitPerMinuteOverride
    ) {
        if (rateLimitPerMinuteOverride != null && rateLimitPerMinuteOverride <= 0)
            throw new IllegalArgumentException("Rate limit must be positive");

        this.rateLimitPerMinuteOverride = rateLimitPerMinuteOverride;
    }

    public void setMonthlyQuotaOverride(Long monthlyQuotaOverride) {
        if (monthlyQuotaOverride != null && monthlyQuotaOverride <= 0)
            throw new IllegalArgumentException("Monthly quota must be positive");

        this.monthlyQuotaOverride = monthlyQuotaOverride;
    }
}
