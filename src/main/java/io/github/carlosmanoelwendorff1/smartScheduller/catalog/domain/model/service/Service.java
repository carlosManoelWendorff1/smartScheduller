// catalog/domain/model/Service.java
package io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Something that can be booked (a haircut, a consultation, a class...). The
 * system deliberately doesn't know what a service "means" (section 11).
 */
@Entity
@Table(name = "service")
public class Service {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Service() {
    }

    private Service(UUID id, UUID tenantId, String name, String description, int durationMinutes, BigDecimal price,
            ServiceStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Service create(UUID tenantId, String name, String description, int durationMinutes,
            BigDecimal price) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required.");
        }
        validateName(name);
        validateDuration(durationMinutes);
        validatePrice(price);

        Instant now = Instant.now();
        return new Service(UUID.randomUUID(), tenantId, name.trim(), trimToNull(description), durationMinutes,
                price, ServiceStatus.ACTIVE, now, now);
    }

    public void update(String name, String description, int durationMinutes, BigDecimal price) {
        validateName(name);
        validateDuration(durationMinutes);
        validatePrice(price);
        this.name = name.trim();
        this.description = trimToNull(description);
        this.durationMinutes = durationMinutes;
        this.price = price;
        touch();
    }

    public void activate() {
        this.status = ServiceStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        this.status = ServiceStatus.INACTIVE;
        touch();
    }

    public boolean isActive() {
        return this.status == ServiceStatus.ACTIVE;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Service name is required.");
        }
    }

    private static void validateDuration(int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero.");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be zero or positive.");
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ServiceStatus getStatus() {
        return status;
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
        if (!(o instanceof Service service))
            return false;
        return Objects.equals(id, service.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}