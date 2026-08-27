// catalog/domain/model/Professional.java
package io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional;

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
 * A person who can perform a service. userId is optional and not a JPA
 * relationship (see Customer's tenantId note).
 */
@Entity
@Table(name = "professional")
public class Professional {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProfessionalStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Professional() {
    }

    private Professional(UUID id, UUID tenantId, UUID userId, String name, ProfessionalStatus status,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Professional create(UUID tenantId, String name, UUID userId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required.");
        }
        validateName(name);

        Instant now = Instant.now();
        return new Professional(UUID.randomUUID(), tenantId, userId, name.trim(), ProfessionalStatus.ACTIVE, now, now);
    }

    public void rename(String newName) {
        validateName(newName);
        this.name = newName.trim();
        touch();
    }

    public void linkUser(UUID userId) {
        this.userId = userId;
        touch();
    }

    public void unlinkUser() {
        this.userId = null;
        touch();
    }

    public void activate() {
        this.status = ProfessionalStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        this.status = ProfessionalStatus.INACTIVE;
        touch();
    }

    public boolean isActive() {
        return this.status == ProfessionalStatus.ACTIVE;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Professional name is required.");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public ProfessionalStatus getStatus() {
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
        if (!(o instanceof Professional that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}