package com.aegis.api_platform.service;

import com.aegis.api_platform.dto.request.LoginRequest;
import com.aegis.api_platform.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}
