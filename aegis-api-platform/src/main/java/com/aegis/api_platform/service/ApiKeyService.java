package com.aegis.api_platform.service;

import com.aegis.api_platform.model.ApiKey;

import java.time.Instant;

public interface ApiKeyService {

    String createKey(Long tenantId, Instant expiresAt);

    void revokeKey(Long keyId);

    ApiKey validateRawKey(String rawKey);
}

