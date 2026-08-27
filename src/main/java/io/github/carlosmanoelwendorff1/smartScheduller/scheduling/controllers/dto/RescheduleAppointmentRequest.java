package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record RescheduleAppointmentRequest(@NotNull @Future Instant startAt, @NotNull @Future Instant endAt) {
}