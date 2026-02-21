package com.aegis.api_platform.mapper;

import com.aegis.api_platform.dto.response.ApiResponse;
import com.aegis.api_platform.model.ApiDefinition;
import org.springframework.stereotype.Component;

@Component
public class ApiDefinitionMapper {

    public ApiResponse toResponse(ApiDefinition api) {
        return new ApiResponse(
                api.getId(),
                api.getTenant().getId(),
                api.getName(),
                api.getPath(),
                api.getHttpMethod(),
                api.getTargetUrl(),
                api.getDescription(),
                api.getStatus(),
                api.getCreatedAt(),
                api.getUpdatedAt()
        );
    }
}
