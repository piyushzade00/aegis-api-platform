package com.aegis.api_platform.service;

import com.aegis.api_platform.dto.request.CreateApiRequest;
import com.aegis.api_platform.dto.request.UpdateApiRequest;
import com.aegis.api_platform.model.ApiDefinition;

import java.util.List;

public interface ApiDefinitionService {

    ApiDefinition createApi(Long tenantId, CreateApiRequest request);

    ApiDefinition updateApi(Long tenantId, Long apiId, UpdateApiRequest request);

    ApiDefinition deactivateApi(Long tenantId, Long apiId);

    ApiDefinition deleteApi(Long tenantId, Long apiId);

    List<ApiDefinition> getAllApis(Long tenantId);

    ApiDefinition getApi(Long tenantId, Long apiId);
}
