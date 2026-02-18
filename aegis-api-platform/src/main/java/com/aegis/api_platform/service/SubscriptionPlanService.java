package com.aegis.api_platform.service;

import com.aegis.api_platform.dto.request.CreateSubscriptionPlanRequest;
import com.aegis.api_platform.dto.request.UpdateSubscriptionPlanRequest;
import com.aegis.api_platform.model.SubscriptionPlan;

import java.util.List;

public interface SubscriptionPlanService {
    SubscriptionPlan createSubscriptionPlan(CreateSubscriptionPlanRequest request);

    SubscriptionPlan updateSubscriptionPlan(Long id, UpdateSubscriptionPlanRequest request);

    SubscriptionPlan archiveSubscriptionPlan(Long id);

    SubscriptionPlan activateSubscriptionPlan(Long id);

    SubscriptionPlan getSubscriptionPlanById(Long id);

    List<SubscriptionPlan> getAllSubscriptionPlans();
}
