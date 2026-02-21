package com.aegis.api_platform.controller;


import com.aegis.api_platform.dto.request.CreateAppUserRequest;
import com.aegis.api_platform.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class AppUserController {

    @RestController
    @RequestMapping("/api/admin/users")
    @RequiredArgsConstructor
    public class UserController {

        private final AppUserService userService;

        @PostMapping
        public ResponseEntity<String> createUser(
                @Valid @RequestBody CreateAppUserRequest request
        ) {
            userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User created successfully");
        }
    }
}
