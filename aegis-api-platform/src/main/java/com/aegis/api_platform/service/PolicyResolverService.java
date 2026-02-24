package com.aegis.api_platform.service;

import com.aegis.api_platform.model.ApiDefinition;
import com.aegis.api_platform.policy.ApiPolicy;

public interface PolicyResolverService {

    ApiPolicy resolve(Long planId, Long apiId);
}
