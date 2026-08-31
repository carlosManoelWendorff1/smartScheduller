package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto;

import java.time.LocalTime;

public record UpsertBusinessHoursRequest(LocalTime startTime, LocalTime endTime, boolean closed) {
}