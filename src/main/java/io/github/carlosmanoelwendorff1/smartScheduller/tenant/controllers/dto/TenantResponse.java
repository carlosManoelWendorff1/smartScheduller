package io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers.dto;

import java.time.Instant;
import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.TenantStatus;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        String timezone,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getTimezone(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt());
    }
}
