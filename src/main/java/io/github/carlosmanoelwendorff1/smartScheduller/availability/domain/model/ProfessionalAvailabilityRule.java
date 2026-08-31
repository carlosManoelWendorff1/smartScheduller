// availability/domain/model/ProfessionalAvailabilityRule.java
package io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A professional's own schedule for a day of week - overrides the tenant's
 * BusinessHours for that professional/day when present. professionalId is a
 * plain UUID, not a JPA relationship (Modulith boundary - catalog owns
 * Professional). Tenant ownership of professionalId is guaranteed at the
 * database level by a composite FK (professional_id, tenant_id), not by a
 * Java call into the catalog module (see V9/V10 migrations).
 */
@Entity
@Table(name = "professional_availability_rule")
public class ProfessionalAvailabilityRule {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean closed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProfessionalAvailabilityRule() {
    }

    private ProfessionalAvailabilityRule(UUID id, UUID tenantId, UUID professionalId, DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime, boolean closed, Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.professionalId = professionalId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.closed = closed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProfessionalAvailabilityRule create(UUID tenantId, UUID professionalId, DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime, boolean closed) {
        if (tenantId == null)
            throw new IllegalArgumentException("tenantId is required.");
        if (professionalId == null)
            throw new IllegalArgumentException("professionalId is required.");
        if (dayOfWeek == null)
            throw new IllegalArgumentException("dayOfWeek is required.");
        validatePeriod(startTime, endTime, closed);

        Instant now = Instant.now();
        LocalTime start = closed ? null : startTime;
        LocalTime end = closed ? null : endTime;
        return new ProfessionalAvailabilityRule(UUID.randomUUID(), tenantId, professionalId, dayOfWeek, start, end,
                closed, now, now);
    }

    public void update(LocalTime startTime, LocalTime endTime, boolean closed) {
        validatePeriod(startTime, endTime, closed);
        this.startTime = closed ? null : startTime;
        this.endTime = closed ? null : endTime;
        this.closed = closed;
        this.updatedAt = Instant.now();
    }

    private static void validatePeriod(LocalTime startTime, LocalTime endTime, boolean closed) {
        if (closed) {
            return;
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime and endTime are required when not closed.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime.");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getProfessionalId() {
        return professionalId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isClosed() {
        return closed;
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
        if (!(o instanceof ProfessionalAvailabilityRule that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}