package com.aegis.api_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiInsightRequest(
        @NotBlank(message = "Question cannot be blank")
        @Size(max = 500, message = "Question too long")
        String question
) {}
