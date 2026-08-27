package io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service;

public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Optional<Service> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Service> findAllByTenantId(UUID tenantId, Pageable pageable);
}