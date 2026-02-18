package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.dto.request.LoginRequest;
import com.aegis.api_platform.dto.response.AuthResponse;
import com.aegis.api_platform.model.AppUser;
import com.aegis.api_platform.security.JwtService;
import com.aegis.api_platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        AppUser user = (AppUser) authentication.getPrincipal();

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getTenant() != null ? user.getTenant().getId() : null
        );
    }
}