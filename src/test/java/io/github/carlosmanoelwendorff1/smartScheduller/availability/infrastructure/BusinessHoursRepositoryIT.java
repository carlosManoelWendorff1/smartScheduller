// availability/infrastructure/BusinessHoursRepositoryIT.java
package io.github.carlosmanoelwendorff1.smartScheduller.availability.infrastructure;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.BusinessHours;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.repository.BusinessHoursRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

class BusinessHoursRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private BusinessHoursRepository businessHoursRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant createTenant(String slug) {
        return tenantRepository.saveAndFlush(Tenant.create("Tenant " + slug, slug, "America/Sao_Paulo"));
    }

    @Test
    void persistsAndReloadsBusinessHours() {
        Tenant tenant = createTenant("bh-repo-a");
        BusinessHours bh = BusinessHours.create(tenant.getId(), DayOfWeek.MONDAY, LocalTime.of(9, 0),
                LocalTime.of(18, 0), false);

        businessHoursRepository.saveAndFlush(bh);

        BusinessHours reloaded = businessHoursRepository.findByTenantIdAndDayOfWeek(tenant.getId(), DayOfWeek.MONDAY)
                .orElseThrow();
        assertThat(reloaded.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void enforcesOneRowPerTenantAndDay() {
        Tenant tenant = createTenant("bh-repo-b");
        businessHoursRepository.saveAndFlush(
                BusinessHours.create(tenant.getId(), DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), false));

        BusinessHours duplicated = BusinessHours.create(tenant.getId(), DayOfWeek.MONDAY, LocalTime.of(10, 0),
                LocalTime.of(19, 0), false);

        assertThatThrownBy(() -> businessHoursRepository.saveAndFlush(duplicated))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAllByTenantIdReturnsOnlyThatTenantsDays() {
        Tenant tenantA = createTenant("bh-repo-c1");
        Tenant tenantB = createTenant("bh-repo-c2");
        businessHoursRepository.saveAndFlush(
                BusinessHours.create(tenantA.getId(), DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0),
                        false));
        businessHoursRepository.saveAndFlush(
                BusinessHours.create(tenantA.getId(), DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0),
                        false));
        businessHoursRepository.saveAndFlush(
                BusinessHours.create(tenantB.getId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(16, 0),
                        false));

        var days = businessHoursRepository.findAllByTenantId(tenantA.getId());

        assertThat(days).hasSize(2);
        assertThat(days).extracting(BusinessHours::getTenantId).containsOnly(tenantA.getId());
    }
}