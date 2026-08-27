package io.github.carlosmanoelwendorff1.smartScheduller.catalog.infrastructure.professional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional.Professional;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.professional.ProfessionalRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

class ProfessionalRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant createTenant(String slug) {
        return tenantRepository.saveAndFlush(Tenant.create("Tenant " + slug, slug, "America/Sao_Paulo"));
    }

    @Test
    void persistsAndReloadsAProfessionalWithoutUserLink() {
        Tenant tenant = createTenant("prof-repo-a");
        Professional professional = Professional.create(tenant.getId(), "Dra. Ana", null);

        professionalRepository.saveAndFlush(professional);

        Professional reloaded = professionalRepository.findByIdAndTenantId(professional.getId(), tenant.getId())
                .orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Dra. Ana");
        assertThat(reloaded.getUserId()).isNull();
    }

    @Test
    void doesNotReturnProfessionalForAnotherTenant() {
        Tenant tenantA = createTenant("prof-repo-b1");
        Tenant tenantB = createTenant("prof-repo-b2");
        Professional professional = Professional.create(tenantA.getId(), "Dra. Ana", null);
        professionalRepository.saveAndFlush(professional);

        assertThat(professionalRepository.findByIdAndTenantId(professional.getId(), tenantB.getId())).isEmpty();
    }

    @Test
    void databaseRejectsProfessionalForUnknownTenant() {
        Professional professional = Professional.create(UUID.randomUUID(), "Dra. Ana", null);

        assertThatThrownBy(() -> professionalRepository.saveAndFlush(professional))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}