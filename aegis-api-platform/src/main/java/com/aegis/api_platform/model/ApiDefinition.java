package com.aegis.api_platform.model;

import com.aegis.api_platform.enums.ApiStatus;
import com.aegis.api_platform.enums.HttpMethod;
import com.aegis.api_platform.util.PathNormalizer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "api_definition",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "path", "http_method"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiDefinition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false)
    private HttpMethod httpMethod;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiStatus status;

    public ApiDefinition(
            Tenant tenant,
            String name,
            String path,
            HttpMethod httpMethod,
            String targetUrl,
            String description,
            boolean isPublic
    ) {
        if (tenant == null)
            throw new IllegalArgumentException("Tenant is required");

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("API name cannot be blank");


        this.tenant = tenant;
        this.name = name.trim();
        this.path = PathNormalizer.normalize(path);
        this.httpMethod = httpMethod;
        this.description = description;
        this.targetUrl = targetUrl.trim();
        this.status = ApiStatus.ACTIVE;
        this.isPublic = isPublic;
    }

    public void deactivate() {
        this.status = ApiStatus.INACTIVE;
    }

    public void activate() {
        this.status = ApiStatus.ACTIVE;
    }

    public void deprecate() {
        this.status = ApiStatus.DEPRECATED;
    }

    public void softDelete() {
        this.status = ApiStatus.DELETED;
    }

    public void updateDetails(String name, String description, boolean isPublic,String targetUrl) {

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("API name cannot be blank");

        if (targetUrl == null || targetUrl.isBlank())
            throw new IllegalArgumentException("Target URL cannot be blank");

        this.name = name.trim();
        this.description = description;
        this.isPublic = isPublic;
        this.targetUrl = targetUrl.trim();
    }
}
