package io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model;

import java.util.UUID;

public record LoginResult(String token, UUID userId, UUID tenantId, String role) {
}