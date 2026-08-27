package io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto;

import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.LoginResult;

public record LoginResponse(String token, UUID userId, UUID tenantId, String role) {
    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.token(), result.userId(), result.tenantId(), result.role());
    }
}