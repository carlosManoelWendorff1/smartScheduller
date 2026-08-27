package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppointmentRequest(
        @NotNull UUID customerId,
        @NotNull UUID serviceId,
        UUID professionalId,
        UUID resourceId,
        @NotNull @Future Instant startAt,
        @NotNull @Future Instant endAt,
        @Size(max = 1000) String notes) {
}