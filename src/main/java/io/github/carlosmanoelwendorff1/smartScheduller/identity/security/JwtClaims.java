package io.github.carlosmanoelwendorff1.smartScheduller.identity.security;

import java.util.UUID;

public record JwtClaims(UUID userId, UUID tenantId, String role) {
}