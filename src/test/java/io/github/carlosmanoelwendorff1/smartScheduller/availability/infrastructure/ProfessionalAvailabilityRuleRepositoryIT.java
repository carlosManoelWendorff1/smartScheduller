package io.github.carlosmanoelwendorff1.smartScheduller.availability.infrastructure;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.ProfessionalAvailabilityRule;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.repository.ProfessionalAvailabilityRuleRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional.Professional;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.professional.ProfessionalRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

class ProfessionalAvailabilityRuleRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ProfessionalAvailabilityRuleRepository ruleRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    private Tenant tenant;
    private Professional professional;

    private void setUpFixtures(String slug) {
        tenant = tenantRepository.saveAndFlush(Tenant.create("Tenant " + slug, slug, "America/Sao_Paulo"));
        professional = professionalRepository.saveAndFlush(Professional.create(tenant.getId(), "Dra. Ana", null));
    }

    @Test
    void persistsAndReloadsARule() {
        setUpFixtures("rule-repo-a");
        var rule = ProfessionalAvailabilityRule.create(tenant.getId(), professional.getId(), DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(17, 0), false);

        ruleRepository.saveAndFlush(rule);

        var reloaded = ruleRepository.findByTenantIdAndProfessionalIdAndDayOfWeek(tenant.getId(), professional.getId(),
                DayOfWeek.MONDAY).orElseThrow();
        assertThat(reloaded.getStartTime()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    void enforcesOneRuledPerProfessionalAndDay() {
        setUpFixtures("rule-repo-b");
        ruleRepository.saveAndFlush(ProfessionalAvailabilityRule.create(tenant.getId(), professional.getId(),
                DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0), false));

        var duplicated = ProfessionalAvailabilityRule.create(tenant.getId(), professional.getId(), DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(18, 0), false);

        assertThatThrownBy(() -> ruleRepository.saveAndFlush(duplicated))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsRuleForUnknownProfessional() {
        setUpFixtures("rule-repo-c");
        var rule = ProfessionalAvailabilityRule.create(tenant.getId(), UUID.randomUUID(), DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(17, 0), false);

        assertThatThrownBy(() -> ruleRepository.saveAndFlush(rule))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsRuleWhenProfessionalBelongsToAnotherTenant() {
        setUpFixtures("rule-repo-d1");
        Tenant otherTenant = tenantRepository.saveAndFlush(Tenant.create("Outro", "rule-repo-d2", "America/Sao_Paulo"));

        // professional belongs to `tenant`, but we claim tenantId = otherTenant - the
        // composite FK (professional_id, tenant_id) must catch this mismatch.
        var rule = ProfessionalAvailabilityRule.create(otherTenant.getId(), professional.getId(), DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(17, 0), false);

        assertThatThrownBy(() -> ruleRepository.saveAndFlush(rule))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAllByTenantIdAndProfessionalIdReturnsOnlyThatProfessionalsRules() {
        setUpFixtures("rule-repo-e");
        Professional otherProfessional = professionalRepository.saveAndFlush(
                Professional.create(tenant.getId(), "Dr. Joao", null));

        ruleRepository.saveAndFlush(ProfessionalAvailabilityRule.create(tenant.getId(), professional.getId(),
                DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0), false));
        ruleRepository.saveAndFlush(ProfessionalAvailabilityRule.create(tenant.getId(), professional.getId(),
                DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(17, 0), false));
        ruleRepository.saveAndFlush(ProfessionalAvailabilityRule.create(tenant.getId(), otherProfessional.getId(),
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), false));

        var rules = ruleRepository.findAllByTenantIdAndProfessionalId(tenant.getId(), professional.getId());

        assertThat(rules).hasSize(2);
        assertThat(rules).extracting(ProfessionalAvailabilityRule::getProfessionalId)
                .containsOnly(professional.getId());
    }
}