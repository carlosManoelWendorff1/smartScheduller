package io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.ServiceStatus;

public record ServiceResponse(UUID id, UUID tenantId, String name, String description, int durationMinutes,
        BigDecimal price, ServiceStatus status, Instant createdAt, Instant updatedAt) {
    public static ServiceResponse from(
            Service s) {
        return new ServiceResponse(s.getId(), s.getTenantId(), s.getName(), s.getDescription(),
                s.getDurationMinutes(), s.getPrice(), (ServiceStatus) s.getStatus(), s.getCreatedAt(),
                s.getUpdatedAt());
    }
}