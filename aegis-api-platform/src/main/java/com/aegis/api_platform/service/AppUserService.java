package com.aegis.api_platform.service;

import com.aegis.api_platform.dto.request.CreateAppUserRequest;
import com.aegis.api_platform.model.AppUser;

public interface AppUserService {

    AppUser createUser(CreateAppUserRequest request);
}
