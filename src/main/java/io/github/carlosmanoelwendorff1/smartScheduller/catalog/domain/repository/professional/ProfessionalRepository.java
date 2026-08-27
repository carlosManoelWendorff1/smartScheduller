package io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.professional;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional.Professional;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {
    Optional<Professional> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Professional> findAllByTenantId(UUID tenantId, Pageable pageable);
}