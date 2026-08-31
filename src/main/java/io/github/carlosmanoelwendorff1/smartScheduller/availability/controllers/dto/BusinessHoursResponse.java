package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.BusinessHours;

public record BusinessHoursResponse(UUID id, UUID tenantId, DayOfWeek dayOfWeek, LocalTime startTime,
        LocalTime endTime, boolean closed, Instant createdAt, Instant updatedAt) {
    public static BusinessHoursResponse from(BusinessHours bh) {
        return new BusinessHoursResponse(bh.getId(), bh.getTenantId(), bh.getDayOfWeek(), bh.getStartTime(),
                bh.getEndTime(), bh.isClosed(), bh.getCreatedAt(), bh.getUpdatedAt());
    }
}