package io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.CustomerStatus;

public record CustomerResponse(
        UUID id, UUID tenantId, String name, String email, String phone, String document,
        LocalDate birthday, CustomerStatus status, Instant createdAt, Instant updatedAt) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(), customer.getTenantId(), customer.getName(), customer.getEmail(),
                customer.getPhone(), customer.getDocument(), customer.getBirthday(), customer.getStatus(),
                customer.getCreatedAt(), customer.getUpdatedAt());
    }
}