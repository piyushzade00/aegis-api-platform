package com.aegis.api_platform.model;

import com.aegis.api_platform.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionPlan extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String planCode;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Long monthlyQuota;

    @Column(nullable = false)
    private Integer rateLimitPerMinute;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public SubscriptionPlan(
            String planCode,
            String name,
            Long monthlyQuota,
            Integer rateLimitPerMinute,
            BigDecimal price,
            String currency
    ) {
        this.planCode = planCode;
        this.name = name;
        this.monthlyQuota = monthlyQuota;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.price = price;
        this.currency = currency;
        this.status = Status.ACTIVE;
    }

    public void archive() {
        this.status = Status.ARCHIVED;
    }

    public void activate() {
        this.status = Status.ACTIVE;
    }

    public void updateDetails(
            String name,
            Long monthlyQuota,
            Integer rateLimitPerMinute,
            String currency
    ) {
        this.name = name;
        this.monthlyQuota = monthlyQuota;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.currency = currency;
    }
}
