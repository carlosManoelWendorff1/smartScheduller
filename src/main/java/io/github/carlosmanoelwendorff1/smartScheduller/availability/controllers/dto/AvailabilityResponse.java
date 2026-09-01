package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.AvailabilityCalculator.TimeSlot;

public record AvailabilityResponse(UUID professionalId, UUID resourceId, LocalDate date,
        List<TimeSlotResponse> slots) {
    public static AvailabilityResponse from(UUID professionalId, UUID resourceId, LocalDate date,
            List<TimeSlot> slots) {
        return new AvailabilityResponse(professionalId, resourceId, date,
                slots.stream().map(TimeSlotResponse::from).toList());
    }
}