package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Optional<Appointment> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Appointment> findAllByTenantId(UUID tenantId, Pageable pageable);
}