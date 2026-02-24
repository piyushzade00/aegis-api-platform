package com.aegis.api_platform.controller;

import com.aegis.api_platform.dto.request.PlanApiConfigRequest;
import com.aegis.api_platform.service.PlanApiConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plan-config")
@RequiredArgsConstructor
public class PlanApiConfigController {

    private final PlanApiConfigService service;

    @PostMapping("/plan/{planId}/api/{apiId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> createOrUpdate(
            @PathVariable Long planId,
            @PathVariable Long apiId,
            @RequestBody PlanApiConfigRequest request
    ) {

        service.createOrUpdate(
                planId,
                apiId,
                request.rateLimitPerMinuteOverride(),
                request.monthlyQuotaOverride()
        );

        return ResponseEntity.ok().build();
    }
}
