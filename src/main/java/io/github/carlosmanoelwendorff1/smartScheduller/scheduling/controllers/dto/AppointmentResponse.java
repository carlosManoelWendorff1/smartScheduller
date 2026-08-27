package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto;

import java.time.Instant;
import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.AppointmentStatus;

public record AppointmentResponse(UUID id, UUID tenantId, UUID customerId, UUID serviceId, UUID professionalId,
        UUID resourceId, Instant startAt, Instant endAt, AppointmentStatus status,
        String notes, Instant createdAt, Instant updatedAt) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(a.getId(), a.getTenantId(), a.getCustomerId(), a.getServiceId(),
                a.getProfessionalId(), a.getResourceId(), a.getStartAt(), a.getEndAt(), a.getStatus(), a.getNotes(),
                a.getCreatedAt(), a.getUpdatedAt());
    }
}