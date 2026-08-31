package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto;

import java.time.LocalTime;

public record UpsertAvailabilityRuleRequest(LocalTime startTime, LocalTime endTime, boolean closed) {
}