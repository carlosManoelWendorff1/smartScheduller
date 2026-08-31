package io.github.carlosmanoelwendorff1.smartScheduller.availability.services;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.BusinessHours;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.ProfessionalAvailabilityRule;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.repository.BusinessHoursRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.repository.ProfessionalAvailabilityRuleRepository;

@ExtendWith(MockitoExtension.class)
class ProfessionalAvailabilityRuleServiceTest {

    @Mock
    private ProfessionalAvailabilityRuleRepository ruleRepository;

    @Mock
    private BusinessHoursRepository businessHoursRepository;

    private ProfessionalAvailabilityRuleService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID professionalId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ProfessionalAvailabilityRuleService(ruleRepository, businessHoursRepository);
    }

    @Test
    void usesProfessionalOwnRuleWhenPresent() {
        var rule = ProfessionalAvailabilityRule.create(tenantId, professionalId, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(17, 0), false);
        when(ruleRepository.findByTenantIdAndProfessionalIdAndDayOfWeek(tenantId, professionalId, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(rule));

        var effective = service.findEffective(tenantId, professionalId, DayOfWeek.MONDAY).orElseThrow();

        assertThat(effective.source()).isEqualTo("PROFESSIONAL");
        assertThat(effective.startTime()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    void fallsBackToTenantBusinessHoursWhenProfessionalHasNoRule() {
        when(ruleRepository.findByTenantIdAndProfessionalIdAndDayOfWeek(tenantId, professionalId, DayOfWeek.MONDAY))
                .thenReturn(Optional.empty());
        var businessHours = BusinessHours.create(tenantId, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0),
                false);
        when(businessHoursRepository.findByTenantIdAndDayOfWeek(tenantId, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(businessHours));

        var effective = service.findEffective(tenantId, professionalId, DayOfWeek.MONDAY).orElseThrow();

        assertThat(effective.source()).isEqualTo("TENANT_DEFAULT");
        assertThat(effective.startTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void returnsEmptyWhenNeitherProfessionalNorTenantHasARuleForThatDay() {
        when(ruleRepository.findByTenantIdAndProfessionalIdAndDayOfWeek(tenantId, professionalId, DayOfWeek.SUNDAY))
                .thenReturn(Optional.empty());
        when(businessHoursRepository.findByTenantIdAndDayOfWeek(tenantId, DayOfWeek.SUNDAY))
                .thenReturn(Optional.empty());

        assertThat(service.findEffective(tenantId, professionalId, DayOfWeek.SUNDAY)).isEmpty();
    }
}