package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.dto.request.CreateApiRequest;
import com.aegis.api_platform.dto.request.UpdateApiRequest;
import com.aegis.api_platform.enums.ApiStatus;
import com.aegis.api_platform.enums.Status;
import com.aegis.api_platform.model.ApiDefinition;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.repository.ApiDefinitionRepository;
import com.aegis.api_platform.repository.TenantRepository;
import com.aegis.api_platform.service.ApiDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiDefinitionServiceImpl implements ApiDefinitionService {

    private final ApiDefinitionRepository apiRepository;
    private final TenantRepository tenantRepository;

    @Override
    public ApiDefinition createApi(Long tenantId, CreateApiRequest request) {

        Tenant tenant = getActiveTenant(tenantId);

        if (apiRepository.existsByTenantIdAndPathAndHttpMethod(
                tenantId,
                request.path(),
                request.httpMethod()
        )) {
            throw new IllegalArgumentException("API already exists for this path and method.");
        }

        ApiDefinition api = new ApiDefinition(
                tenant,
                request.name(),
                request.path(),
                request.httpMethod(),
                request.targetUrl(),
                request.description(),
                request.isPublic()
        );

        return apiRepository.save(api);
    }

    @Override
    public ApiDefinition updateApi(Long tenantId, Long apiId, UpdateApiRequest request) {

        ApiDefinition api = getApiOrThrow(tenantId, apiId);

        if (api.getStatus() == ApiStatus.DELETED)
            throw new IllegalStateException("Deleted API cannot be updated.");

        api.updateDetails(
                request.name(),
                request.description(),
                request.isPublic(),
                request.targetUrl()
        );

        return api;
    }

    @Override
    public ApiDefinition deactivateApi(Long tenantId, Long apiId) {

        ApiDefinition api = getApiOrThrow(tenantId, apiId);

        api.deactivate();

        return api;
    }

    @Override
    public ApiDefinition deleteApi(Long tenantId, Long apiId) {

        ApiDefinition api = getApiOrThrow(tenantId, apiId);

        api.softDelete();

        return api;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiDefinition> getAllApis(Long tenantId) {
        return apiRepository.findAllByTenantIdAndStatusNot(
                tenantId,
                ApiStatus.DELETED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiDefinition getApi(Long tenantId, Long apiId) {
        return getApiOrThrow(tenantId, apiId);
    }

    // -------- PRIVATE METHODS ----------

    private Tenant getActiveTenant(Long tenantId) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found."));

        if (tenant.getStatus() != Status.ACTIVE)
            throw new IllegalStateException("Tenant is not active.");

        return tenant;
    }

    private ApiDefinition getApiOrThrow(Long tenantId, Long apiId) {

        ApiDefinition api = apiRepository.findById(apiId)
                .orElseThrow(() -> new IllegalArgumentException("API not found."));

        if (!api.getTenant().getId().equals(tenantId))
            throw new IllegalStateException("API does not belong to this tenant.");

        return api;
    }
}