package com.aegis.api_platform.service.impl;

import com.aegis.api_platform.dto.request.CreateAppUserRequest;
import com.aegis.api_platform.enums.Role;
import com.aegis.api_platform.enums.Status;
import com.aegis.api_platform.model.AppUser;
import com.aegis.api_platform.model.Tenant;
import com.aegis.api_platform.repository.AppUserRepository;
import com.aegis.api_platform.repository.TenantRepository;
import com.aegis.api_platform.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AppUser createUser(CreateAppUserRequest request) {

        if (request.role() == Role.SYSTEM_ADMIN) {
            throw new IllegalArgumentException("SYSTEM_ADMIN cannot be created via API");
        }

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Tenant tenant = null;

        if (request.role() == Role.TENANT_ADMIN) {

            if (request.tenantId() == null) {
                throw new IllegalArgumentException("Tenant ID required for TENANT_ADMIN");
            }

            tenant = tenantRepository.findById(request.tenantId())
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

            if (tenant.getStatus() != Status.ACTIVE) {
                throw new IllegalStateException("Cannot assign user to inactive tenant");
            }
        }

        AppUser user = new AppUser(
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password()),
                request.role(),
                tenant,
                Status.ACTIVE
        );

        return userRepository.save(user);
    }
}
