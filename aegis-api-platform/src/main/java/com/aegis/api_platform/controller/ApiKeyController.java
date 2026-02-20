package com.aegis.api_platform.controller;

import com.aegis.api_platform.security.SecurityUtils;
import com.aegis.api_platform.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/tenant/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<String> createKey(
            @RequestParam(required = false) Long expiresInDays) {

        Long tenantId = securityUtils.getCurrentTenantId();

        Instant expiry = null;

        if (expiresInDays != null) {
            expiry = Instant.now().plus(expiresInDays, ChronoUnit.DAYS);
        }

        String rawKey = apiKeyService.createKey(tenantId, expiry);

        return ResponseEntity.status(HttpStatus.CREATED).body(rawKey);
    }

    @PatchMapping("/{keyId}/revoke")
    public ResponseEntity<Void> revoke(@PathVariable Long keyId) {
        apiKeyService.revokeKey(keyId);
        return ResponseEntity.noContent().build();
    }
}
