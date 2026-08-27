package io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.model.resource;

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
 * A room, chair, piece of equipment, vehicle... "type" is free text on purpose
 * (section 13).
 */
@Entity
@Table(name = "resource")
public class Resource {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Resource() {
    }

    private Resource(UUID id, UUID tenantId, String name, String type, ResourceStatus status, Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Resource create(UUID tenantId, String name, String type) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required.");
        }
        validateName(name);
        validateType(type);

        Instant now = Instant.now();
        return new Resource(UUID.randomUUID(), tenantId, name.trim(), type.trim(), ResourceStatus.ACTIVE, now, now);
    }

    public void update(String name, String type) {
        validateName(name);
        validateType(type);
        this.name = name.trim();
        this.type = type.trim();
        touch();
    }

    public void activate() {
        this.status = ResourceStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        this.status = ResourceStatus.INACTIVE;
        touch();
    }

    public boolean isActive() {
        return this.status == ResourceStatus.ACTIVE;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Resource name is required.");
        }
    }

    private static void validateType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Resource type is required.");
        }
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

    public String getType() {
        return type;
    }

    public ResourceStatus getStatus() {
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
        if (!(o instanceof Resource that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}