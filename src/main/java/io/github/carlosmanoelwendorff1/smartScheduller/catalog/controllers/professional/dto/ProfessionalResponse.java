package io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.dto;

import java.time.Instant;
import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional.Professional;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional.ProfessionalStatus;

public record ProfessionalResponse(UUID id, UUID tenantId, UUID userId, String name, ProfessionalStatus status,
        Instant createdAt, Instant updatedAt) {
    public static ProfessionalResponse from(Professional p) {
        return new ProfessionalResponse(p.getId(), p.getTenantId(), p.getUserId(), p.getName(), p.getStatus(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}