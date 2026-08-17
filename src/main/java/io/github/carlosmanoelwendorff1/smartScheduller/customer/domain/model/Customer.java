// customer/domain/model/Customer.java
package io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Customer represents a person who uses a tenant's services (e.g. a
 * barbershop's client, a clinic's patient). Deliberately generic: no
 * business-specific fields (see master instructions section 10). Anything
 * business-specific belongs in future custom fields, not here.
 * <p>
 * {@code tenantId} is a plain UUID, not a JPA relationship to the Tenant
 * entity. The customer module must not depend on tenant's Java classes
 * (Spring Modulith boundary) - referential integrity for tenantId is
 * enforced at the database level instead (FK in the migration).
 */
@Entity
@Table(name = "customer")
public class Customer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 50)
    private String document;

    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Customer() {
        // required by JPA
    }

    private Customer(UUID id, UUID tenantId, String name, String email, String phone, String document,
            LocalDate birthday, CustomerStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.document = document;
        this.birthday = birthday;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Customer create(UUID tenantId, String name, String email, String phone, String document,
            LocalDate birthday) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required.");
        }
        validateName(name);
        String normalizedEmail = validateAndNormalizeEmail(email);
        validateBirthday(birthday);

        Instant now = Instant.now();
        return new Customer(UUID.randomUUID(), tenantId, name.trim(), normalizedEmail, trimToNull(phone),
                trimToNull(document), birthday, CustomerStatus.ACTIVE, now, now);
    }

    public void rename(String newName) {
        validateName(newName);
        this.name = newName.trim();
        touch();
    }

    public void updateProfile(String email, String phone, String document, LocalDate birthday) {
        validateBirthday(birthday);
        this.email = validateAndNormalizeEmail(email);
        this.phone = trimToNull(phone);
        this.document = trimToNull(document);
        this.birthday = birthday;
        touch();
    }

    public void archive() {
        this.status = CustomerStatus.INACTIVE;
        touch();
    }

    public void activate() {
        this.status = CustomerStatus.ACTIVE;
        touch();
    }

    public boolean isActive() {
        return this.status == CustomerStatus.ACTIVE;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (name.trim().length() > 150) {
            throw new IllegalArgumentException("Customer name must be at most 150 characters.");
        }
    }

    private static String validateAndNormalizeEmail(String email) {
        String trimmed = trimToNull(email);
        if (trimmed == null) {
            return null;
        }
        String normalized = trimmed.toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        return normalized;
    }

    private static void validateBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birthday cannot be in the future.");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
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

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDocument() {
        return document;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public CustomerStatus getStatus() {
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
        if (!(o instanceof Customer customer))
            return false;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}