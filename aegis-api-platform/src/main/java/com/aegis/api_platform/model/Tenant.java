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

    public Tenant(String name, Status status){
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant name cannot be empty or blank.");
        }

        if (status == null) {
            throw new IllegalArgumentException("Tenant status cannot be null.");
        }

        this.name = name;
        this.status = status;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }
}
