// availability/domain/repository/ProfessionalAvailabilityRuleRepository.java
package io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.ProfessionalAvailabilityRule;

public interface ProfessionalAvailabilityRuleRepository extends JpaRepository<ProfessionalAvailabilityRule, UUID> {
    Optional<ProfessionalAvailabilityRule> findByTenantIdAndProfessionalIdAndDayOfWeek(UUID tenantId,
            UUID professionalId, DayOfWeek dayOfWeek);

    List<ProfessionalAvailabilityRule> findAllByTenantIdAndProfessionalId(UUID tenantId, UUID professionalId);
}