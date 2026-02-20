package com.aegis.api_platform.service;

import com.aegis.api_platform.dto.request.CreateApiRequest;
import com.aegis.api_platform.dto.request.UpdateApiRequest;
import com.aegis.api_platform.model.ApiDefinition;

import java.util.List;

public interface ApiDefinitionService {

    ApiDefinition createApi(CreateApiRequest request);

    ApiDefinition updateApi(Long apiId, UpdateApiRequest request);

    ApiDefinition deactivateApi(Long apiId);

    ApiDefinition deleteApi(Long apiId);

    List<ApiDefinition> getAllApis();

    ApiDefinition getApi(Long apiId);

    ApiDefinition resolveApi(String path, String method);
}
