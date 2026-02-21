package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.dto.request.CreateApiRequest;
import com.aegis.api_platform.dto.request.UpdateApiRequest;
import com.aegis.api_platform.enums.ApiStatus;
import com.aegis.api_platform.enums.HttpMethod;
import com.aegis.api_platform.enums.Status;
import com.aegis.api_platform.model.ApiDefinition;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.repository.ApiDefinitionRepository;
import com.aegis.api_platform.repository.TenantRepository;
import com.aegis.api_platform.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    @Override
    public ApiDefinition createApi(CreateApiRequest request) {

        Long tenantId = securityUtils.getCurrentTenantId();

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
                request.description()
        );

        return apiRepository.save(api);
    }

    @Override
    public ApiDefinition updateApi(Long apiId, UpdateApiRequest request) {

        Long tenantId = securityUtils.getCurrentTenantId();

        ApiDefinition api = getApiOrThrow(tenantId, apiId);

        if (api.getStatus() == ApiStatus.DELETED)
            throw new IllegalStateException("Deleted API cannot be updated.");

        api.updateDetails(
                request.name(),
                request.description(),
                request.targetUrl()
        );

        return api;
    }

    @Override
    public ApiDefinition deactivateApi(Long apiId) {

        Long tenantId = securityUtils.getCurrentTenantId();

        ApiDefinition api = getApiOrThrow(tenantId, apiId);

        api.deactivate();

        return api;
    }

    @Override
    public ApiDefinition deleteApi(Long apiId) {

        Long tenantId = securityUtils.getCurrentTenantId();

        ApiDefinition api = getApiOrThrow(tenantId, apiId);

        api.softDelete();

        return api;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiDefinition> getAllApis() {

        Long tenantId = securityUtils.getCurrentTenantId();

        return apiRepository.findAllByTenantIdAndStatusNot(
                tenantId,
                ApiStatus.DELETED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiDefinition getApi(Long apiId) {

        Long tenantId = securityUtils.getCurrentTenantId();

        return getApiOrThrow(tenantId, apiId);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiDefinition resolveApi(Long tenantId, String path, String method) {

        String normalizedPath = normalizePath(path);

        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase().trim());

        ApiDefinition api = apiRepository
                .findByTenantIdAndPathAndHttpMethodAndStatus(
                        tenantId,
                        normalizedPath,
                        httpMethod,
                        ApiStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("API not found"));

        if (api.getTenant().getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Tenant is not active");
        }

        return api;
    }

    // -------- PRIVATE METHODS ----------

    private Tenant getActiveTenant(Long tenantId) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found."));

        if (tenant.getStatus() == Status.DELETED) {
            throw new IllegalStateException("Deleted tenant cannot perform operations.");
        }

        if (tenant.getStatus() == Status.SUSPENDED) {
            throw new IllegalStateException("Suspended tenant cannot perform operations.");
        }

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

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Invalid path");
        }

        String cleaned = path.trim();

        if (!cleaned.startsWith("/")) {
            cleaned = "/" + cleaned;
        }

        return cleaned;
    }
}