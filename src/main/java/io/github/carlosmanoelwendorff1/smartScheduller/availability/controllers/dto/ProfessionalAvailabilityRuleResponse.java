package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.ProfessionalAvailabilityRule;

public record ProfessionalAvailabilityRuleResponse(UUID id, UUID tenantId, UUID professionalId, DayOfWeek dayOfWeek,
        LocalTime startTime, LocalTime endTime, boolean closed,
        Instant createdAt, Instant updatedAt) {
    public static ProfessionalAvailabilityRuleResponse from(ProfessionalAvailabilityRule rule) {
        return new ProfessionalAvailabilityRuleResponse(rule.getId(), rule.getTenantId(), rule.getProfessionalId(),
                rule.getDayOfWeek(), rule.getStartTime(), rule.getEndTime(), rule.isClosed(), rule.getCreatedAt(),
                rule.getUpdatedAt());
    }
}