package com.aegis.api_platform.gateway;

import com.aegis.api_platform.model.ApiKey;
import com.aegis.api_platform.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/gateway");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String rawKey = request.getHeader("X-API-KEY");

        if (rawKey == null || rawKey.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API key missing");
            return;
        }

        try {
            ApiKey apiKey = apiKeyService.validateRawKey(rawKey);

            // Attach tenant to request context
            request.setAttribute("tenantId", apiKey.getTenant().getId());
            request.setAttribute("apiKeyId", apiKey.getId());

        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
