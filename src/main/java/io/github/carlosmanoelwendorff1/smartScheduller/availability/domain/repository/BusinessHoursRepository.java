package io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.BusinessHours;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, UUID> {
    Optional<BusinessHours> findByTenantIdAndDayOfWeek(UUID tenantId, DayOfWeek dayOfWeek);

    List<BusinessHours> findAllByTenantId(UUID tenantId);
}