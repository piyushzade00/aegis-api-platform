package com.aegis.api_platform.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String email,
        String role,
        Long tenantId
) {}
