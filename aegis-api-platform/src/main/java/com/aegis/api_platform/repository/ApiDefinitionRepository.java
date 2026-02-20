package com.aegis.api_platform.repository;

import com.aegis.api_platform.enums.ApiStatus;
import com.aegis.api_platform.enums.HttpMethod;
import com.aegis.api_platform.model.ApiDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiDefinitionRepository extends JpaRepository<ApiDefinition, Long> {

    boolean existsByTenantIdAndPathAndHttpMethod(
            Long tenantId,
            String path,
            HttpMethod httpMethod
    );

    List<ApiDefinition> findByTenantId(Long tenantId);

    List<ApiDefinition> findAllByTenantIdAndStatusNot(
            Long tenantId,
            ApiStatus status
    );

    Optional<ApiDefinition> findByTenantIdAndPathAndHttpMethodAndStatus(
            Long tenantId,
            String path,
            HttpMethod httpMethod,
            ApiStatus status
    );

}
