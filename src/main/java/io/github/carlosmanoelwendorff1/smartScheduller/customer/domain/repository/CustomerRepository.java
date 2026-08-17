// customer/domain/repository/CustomerRepository.java
package io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Customer> findAllByTenantId(UUID tenantId, Pageable pageable);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
}