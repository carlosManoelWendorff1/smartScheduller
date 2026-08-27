// identity/controllers/dto/UserResponse.java
package io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto;

import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.Role;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.User;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, UUID tenantId, String name, String email, Role role, UserStatus status,
        Instant createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getTenantId(), user.getName(), user.getEmail(), user.getRole(),
                user.getStatus(), user.getCreatedAt());
    }
}