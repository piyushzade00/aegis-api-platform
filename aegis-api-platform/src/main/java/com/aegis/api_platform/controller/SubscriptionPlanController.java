package com.aegis.api_platform.controller;

import com.aegis.api_platform.dto.request.CreateSubscriptionPlanRequest;
import com.aegis.api_platform.dto.request.UpdateSubscriptionPlanRequest;
import com.aegis.api_platform.dto.response.SubscriptionPlanResponse;
import com.aegis.api_platform.mapper.SubscriptionPlanMapper;
import com.aegis.api_platform.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService service;
    private final SubscriptionPlanMapper mapper;

    @PostMapping
    public ResponseEntity<SubscriptionPlanResponse> create(
            @Valid @RequestBody CreateSubscriptionPlanRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(service.createSubscriptionPlan(request)));
    }

    @PutMapping("/{id}")
    public SubscriptionPlanResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request
    ) {
        return mapper.toResponse(service.updateSubscriptionPlan(id, request));
    }

    @PatchMapping("/{id}/archive")
    public SubscriptionPlanResponse archive(@PathVariable Long id) {
        return mapper.toResponse(service.archiveSubscriptionPlan(id));
    }

    @PatchMapping("/{id}/activate")
    public SubscriptionPlanResponse activate(@PathVariable Long id) {
        return mapper.toResponse(service.activateSubscriptionPlan(id));
    }

    @GetMapping("/{id}")
    public SubscriptionPlanResponse get(@PathVariable Long id) {
        return mapper.toResponse(service.getSubscriptionPlanById(id));
    }

    @GetMapping
    public List<SubscriptionPlanResponse> getAll() {
        return service.getAllSubscriptionPlans()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
