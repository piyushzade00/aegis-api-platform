package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.enums.ApiKeyStatus;
import com.aegis.api_platform.enums.Status;
import com.aegis.api_platform.model.ApiKey;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.repository.ApiKeyRepository;
import com.aegis.api_platform.repository.TenantRepository;
import com.aegis.api_platform.service.ApiKeyService;
import com.aegis.api_platform.util.ApiKeyHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiKeyServiceImpl implements ApiKeyService {

    private final TenantRepository tenantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyHasher apiKeyHasher;

    @Override
    public String createKey(Long tenantId, Instant expiresAt) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        if (tenant.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Cannot create API key for inactive tenant");
        }

        if (tenant.getSubscriptionPlan().getStatus() == Status.ARCHIVED) {
            throw new IllegalStateException("Cannot create API key for archived plan");
        }

        String rawKey = generateRawKey();
        String hashedKey = apiKeyHasher.hash(rawKey);

        ApiKey apiKey = new ApiKey(tenant, hashedKey, expiresAt);

        apiKeyRepository.save(apiKey);

        return rawKey; // returned only once
    }

    @Override
    public void revokeKey(Long keyId) {

        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));

        apiKey.revoke();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiKey validateRawKey(String rawKey) {

        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalArgumentException("API key missing");
        }

        String hashed = apiKeyHasher.hash(rawKey);

        ApiKey apiKey = apiKeyRepository
                .findByHashedKeyAndStatus(hashed, ApiKeyStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Invalid API key"));

        if (apiKey.isExpired()) {
            throw new IllegalStateException("API key expired");
        }

        if (apiKey.getTenant().getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Tenant inactive");
        }

        return apiKey;
    }

    private String generateRawKey() {
        return "ak_live_" + UUID.randomUUID().toString().replace("-", "");
    }
}
