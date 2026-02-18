package com.aegis.api_platform.mapper;

import com.aegis.api_platform.dto.response.TenantResponse;
import com.aegis.api_platform.model.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

    public TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
