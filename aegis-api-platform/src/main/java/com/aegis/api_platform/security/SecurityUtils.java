package com.aegis.api_platform.security;

import com.aegis.api_platform.model.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public Long getCurrentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }

        AppUser userDetails = (AppUser) authentication.getPrincipal();

        if (userDetails.getTenant().getId() == null) {
            throw new IllegalStateException("User is not associated with any tenant.");
        }

        return userDetails.getTenant().getId();
    }

    public String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No role found."))
                .getAuthority();
    }
}
