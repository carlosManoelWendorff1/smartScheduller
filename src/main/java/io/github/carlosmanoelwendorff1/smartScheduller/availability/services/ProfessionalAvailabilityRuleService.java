package io.github.carlosmanoelwendorff1.smartScheduller.availability.services;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.BusinessHours;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.ProfessionalAvailabilityRule;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.repository.BusinessHoursRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.repository.ProfessionalAvailabilityRuleRepository;

@Service
@Transactional
public class ProfessionalAvailabilityRuleService {

    private final ProfessionalAvailabilityRuleRepository ruleRepository;
    private final BusinessHoursRepository businessHoursRepository;

    public ProfessionalAvailabilityRuleService(ProfessionalAvailabilityRuleRepository ruleRepository,
            BusinessHoursRepository businessHoursRepository) {
        this.ruleRepository = ruleRepository;
        this.businessHoursRepository = businessHoursRepository;
    }

    public ProfessionalAvailabilityRule upsert(UUID tenantId, UUID professionalId, DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime, boolean closed) {
        Optional<ProfessionalAvailabilityRule> existing = ruleRepository
                .findByTenantIdAndProfessionalIdAndDayOfWeek(tenantId, professionalId, dayOfWeek);

        if (existing.isPresent()) {
            ProfessionalAvailabilityRule rule = existing.get();
            rule.update(startTime, endTime, closed);
            return rule;
        }

        ProfessionalAvailabilityRule rule = ProfessionalAvailabilityRule.create(tenantId, professionalId, dayOfWeek,
                startTime, endTime, closed);
        try {
            return ruleRepository.save(rule);
        } catch (DataIntegrityViolationException ex) {
            // Composite FK (professional_id, tenant_id) rejected this - professionalId
            // doesn't exist, or exists under a different tenant. Same signal either way.
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<ProfessionalAvailabilityRule> findAll(UUID tenantId, UUID professionalId) {
        return ruleRepository.findAllByTenantIdAndProfessionalId(tenantId, professionalId);
    }

    public void delete(UUID tenantId, UUID professionalId, DayOfWeek dayOfWeek) {
        ruleRepository.findByTenantIdAndProfessionalIdAndDayOfWeek(tenantId, professionalId, dayOfWeek)
                .ifPresent(ruleRepository::delete);
    }

    /**
     * The actual fallback used by the future AvailabilityEngine: the
     * professional's own rule for that day, or the tenant's BusinessHours for
     * that day if the professional has none, or empty if neither exists
     * (meaning: no configured hours at all for that day - treat as closed).
     */
    @Transactional(readOnly = true)
    public Optional<EffectiveAvailability> findEffective(UUID tenantId, UUID professionalId, DayOfWeek dayOfWeek) {
        Optional<ProfessionalAvailabilityRule> own = ruleRepository
                .findByTenantIdAndProfessionalIdAndDayOfWeek(tenantId, professionalId, dayOfWeek);
        if (own.isPresent()) {
            ProfessionalAvailabilityRule rule = own.get();
            return Optional.of(new EffectiveAvailability(rule.getStartTime(), rule.getEndTime(), rule.isClosed(),
                    "PROFESSIONAL"));
        }

        Optional<BusinessHours> tenantDefault = businessHoursRepository.findByTenantIdAndDayOfWeek(tenantId, dayOfWeek);
        return tenantDefault.map(bh -> new EffectiveAvailability(bh.getStartTime(), bh.getEndTime(), bh.isClosed(),
                "TENANT_DEFAULT"));
    }

    public record EffectiveAvailability(LocalTime startTime, LocalTime endTime, boolean closed, String source) {
    }
}