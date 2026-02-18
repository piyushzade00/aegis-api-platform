package com.aegis.api_platform.controller;

import com.aegis.api_platform.dto.request.CreateTenantRequest;
import com.aegis.api_platform.dto.response.TenantResponse;
import com.aegis.api_platform.mapper.TenantMapper;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody CreateTenantRequest request
    ) {
        Tenant tenant = tenantService.createTenant(request.name(), request.subscriptionPlanId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tenantMapper.toResponse(tenant));
    }

    @GetMapping("/{id}")
    public TenantResponse getTenant(@PathVariable Long id) {
        return tenantMapper.toResponse(tenantService.getTenant(id));
    }

    @GetMapping
    public List<TenantResponse> getAllTenants() {
        return tenantService.getAllTenants()
                .stream()
                .map(tenantMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/suspend")
    public TenantResponse suspend(@PathVariable Long id) {
        return tenantMapper.toResponse(tenantService.suspendTenant(id));
    }

    @PatchMapping("/{id}/activate")
    public TenantResponse activate(@PathVariable Long id) {
        return tenantMapper.toResponse(tenantService.activateTenant(id));
    }

    @DeleteMapping("/{id}")
    public TenantResponse delete(@PathVariable Long id) {
        return tenantMapper.toResponse(tenantService.deleteTenant(id));
    }
}
