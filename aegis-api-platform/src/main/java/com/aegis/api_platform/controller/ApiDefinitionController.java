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

    @PostMapping
    public ApiResponse create(@Valid @RequestBody CreateApiRequest request) {
        return mapper.toResponse(
                apiService.createApi(request)
        );
    }

    @GetMapping
    public List<ApiResponse> getAll() {
        return apiService.getAllApis()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ApiResponse get(@PathVariable Long id) {
        return mapper.toResponse(
                apiService.getApi(id)
        );
    }

    @PatchMapping("/{id}")
    public ApiResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApiRequest request
    ) {
        return mapper.toResponse(
                apiService.updateApi(id, request)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse deactivate(@PathVariable Long id) {
        return mapper.toResponse(
                apiService.deactivateApi(id)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        return mapper.toResponse(
                apiService.deleteApi(id)
        );
    }
}

