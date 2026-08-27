// scheduling/domain/model/Appointment.java
package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Version.touch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The core of the system. professionalId/resourceId are optional plain
 * UUIDs, not JPA relationships - scheduling doesn't depend on catalog's or
 * resource's Java classes (Modulith boundary); referential integrity is a
 * database-level FK (see V7 migration).
 * <p>
 * No conflict-checking here yet (Fase 5 - Availability Engine). This is
 * intentionally "manual creation only" per roadmap Fase 4.
 */
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "professional_id")
    private UUID professionalId;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Appointment() {
    }

    private Appointment(UUID id, UUID tenantId, UUID customerId, UUID serviceId, UUID professionalId,
            UUID resourceId, Instant startAt, Instant endAt, AppointmentStatus status, String notes,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.professionalId = professionalId;
        this.resourceId = resourceId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Appointment create(UUID tenantId, UUID customerId, UUID serviceId, UUID professionalId,
            UUID resourceId, Instant startAt, Instant endAt, String notes) {
        if (tenantId == null)
            throw new IllegalArgumentException("tenantId is required.");
        if (customerId == null)
            throw new IllegalArgumentException("customerId is required.");
        if (serviceId == null)
            throw new IllegalArgumentException("serviceId is required.");
        validatePeriod(startAt, endAt);

        Instant now = Instant.now();
        return new Appointment(UUID.randomUUID(), tenantId, customerId, serviceId, professionalId, resourceId,
                startAt, endAt, AppointmentStatus.PENDING, trimToNull(notes), now, now);
    }

    public void reschedule(Instant newStart, Instant newEnd) {
        ensureMutable("rescheduled");
        validatePeriod(newStart, newEnd);
        this.startAt = newStart;
        this.endAt = newEnd;
        touch();
    }

    public void updateNotes(String notes) {
        this.notes = trimToNull(notes);
        touch();
    }

    public void confirm() {
        if (this.status != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only a PENDING appointment can be confirmed.");
        }
        this.status = AppointmentStatus.CONFIRMED;
        touch();
    }

    public void cancel() {
        ensureMutable("cancelled");
        this.status = AppointmentStatus.CANCELLED;
        touch();
    }

    public void complete() {
        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only a CONFIRMED appointment can be completed.");
        }
        this.status = AppointmentStatus.COMPLETED;
        touch();
    }

    public void markNoShow() {
        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only a CONFIRMED appointment can be marked as no-show.");
        }
        this.status = AppointmentStatus.NO_SHOW;
        touch();
    }

    private void ensureMutable(String action) {
        if (this.status == AppointmentStatus.CANCELLED || this.status == AppointmentStatus.COMPLETED
                || this.status == AppointmentStatus.NO_SHOW) {
            throw new IllegalStateException("Appointment cannot be " + action + " from status " + this.status + ".");
        }
    }

    private static void validatePeriod(Instant startAt, Instant endAt) {
        if (startAt == null)
            throw new IllegalArgumentException("startAt is required.");
        if (endAt == null)
            throw new IllegalArgumentException("endAt is required.");
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("endAt must be after startAt.");
        }
    }

    private static String trimToNull(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public UUID getProfessionalId() {
        return professionalId;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Appointment that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}