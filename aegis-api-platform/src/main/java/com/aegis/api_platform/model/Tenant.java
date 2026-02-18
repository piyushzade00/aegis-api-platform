package com.aegis.api_platform.model;

import com.aegis.api_platform.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;


    public Tenant(String name, Status status, SubscriptionPlan subscriptionPlan){
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant name cannot be empty or blank.");
        }

        if (status == null) {
            throw new IllegalArgumentException("Tenant status cannot be null.");
        }

        if (subscriptionPlan == null) {
            throw new IllegalArgumentException("Tenant subscription plan cannot be null.");
        }

        this.name = name;
        this.status = status;
        this.subscriptionPlan = subscriptionPlan;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }
}
