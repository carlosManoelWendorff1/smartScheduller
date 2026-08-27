package io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.repository.resource;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.model.resource.Resource;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    Optional<Resource> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Resource> findAllByTenantId(UUID tenantId, Pageable pageable);
}