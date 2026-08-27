// identity/controllers/UserController.java
package io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto.RegisterUserRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto.UserResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.User;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.services.UserService;
import jakarta.validation.Valid;

/**
 * TEMPORARY: open with no auth (bootstrap problem - see explanation).
 * Lock this down to an ADMIN-only, invite-based flow in Fase 10 (SaaS).
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@PathVariable UUID tenantId,
            @Valid @RequestBody RegisterUserRequest request) {
        User user = userService.register(tenantId, request.name(), request.email(), request.password(),
                request.role());
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/v1/tenants/" + tenantId + "/users/" + response.id()))
                .body(response);
    }
}