package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto;

import java.time.LocalTime;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.AvailabilityCalculator.TimeSlot;

public record TimeSlotResponse(LocalTime time, boolean available) {
    public static TimeSlotResponse from(TimeSlot slot) {
        return new TimeSlotResponse(slot.time(), slot.available());
    }
}