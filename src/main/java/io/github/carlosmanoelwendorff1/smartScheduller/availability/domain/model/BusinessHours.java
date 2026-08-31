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
 * Tenant-wide default schedule, one row per day of week. Not consulted
 * directly by the (future) AvailabilityEngine - it's the fallback that
 * ProfessionalAvailabilityRule uses when a professional has no rule of
 * their own for a given day (see ProfessionalAvailabilityRuleService).
 */
@Entity
@Table(name = "business_hours")
public class BusinessHours {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

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

    protected BusinessHours() {
    }

    private BusinessHours(UUID id, UUID tenantId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
            boolean closed, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.closed = closed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BusinessHours create(UUID tenantId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
            boolean closed) {
        if (tenantId == null)
            throw new IllegalArgumentException("tenantId is required.");
        if (dayOfWeek == null)
            throw new IllegalArgumentException("dayOfWeek is required.");
        validatePeriod(startTime, endTime, closed);

        Instant now = Instant.now();
        LocalTime start = closed ? null : startTime;
        LocalTime end = closed ? null : endTime;
        return new BusinessHours(UUID.randomUUID(), tenantId, dayOfWeek, start, end, closed, now, now);
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
        if (!(o instanceof BusinessHours that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}