package com.aegis.api_platform.controller;

import com.aegis.api_platform.dto.request.CreateApiRequest;
import com.aegis.api_platform.dto.request.UpdateApiRequest;
import com.aegis.api_platform.dto.response.ApiResponse;
import com.aegis.api_platform.mapper.ApiDefinitionMapper;
import com.aegis.api_platform.service.ApiDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant/apis")
@RequiredArgsConstructor
public class ApiDefinitionController {

    private final ApiDefinitionService apiService;
    private final ApiDefinitionMapper mapper;

    // Simulated tenantId for now
    private static final Long TENANT_ID = 1L;

    @PostMapping
    public ApiResponse create(@Valid @RequestBody CreateApiRequest request) {
        return mapper.toResponse(
                apiService.createApi(TENANT_ID, request)
        );
    }

    @GetMapping
    public List<ApiResponse> getAll() {
        return apiService.getAllApis(TENANT_ID)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ApiResponse get(@PathVariable Long id) {
        return mapper.toResponse(
                apiService.getApi(TENANT_ID, id)
        );
    }

    @PatchMapping("/{id}")
    public ApiResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApiRequest request
    ) {
        return mapper.toResponse(
                apiService.updateApi(TENANT_ID, id, request)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse deactivate(@PathVariable Long id) {
        return mapper.toResponse(
                apiService.deactivateApi(TENANT_ID, id)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        return mapper.toResponse(
                apiService.deleteApi(TENANT_ID, id)
        );
    }
}

