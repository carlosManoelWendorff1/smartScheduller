package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto;

import jakarta.validation.constraints.Size;

public record UpdateAppointmentNotesRequest(@Size(max = 1000) String notes) {
}