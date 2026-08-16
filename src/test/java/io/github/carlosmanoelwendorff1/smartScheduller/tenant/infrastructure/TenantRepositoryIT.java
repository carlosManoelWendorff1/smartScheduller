package io.github.carlosmanoelwendorff1.smartScheduller.tenant.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

/**
 * Valida que o mapeamento JPA + a migration V1 (tabela tenant) funcionam
 * corretamente contra um Postgres real, incluindo a constraint de
 * unicidade de slug definida no banco.
 */
class TenantRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void persistsAndReloadsATenant() {
        Tenant tenant = Tenant.create("Oficina Central", "oficina-central", "America/Sao_Paulo");

        tenantRepository.saveAndFlush(tenant);

        Tenant reloaded = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Oficina Central");
        assertThat(reloaded.getSlug()).isEqualTo("oficina-central");
        assertThat(reloaded.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    void findsTenantBySlug() {
        Tenant tenant = Tenant.create("Estudio Fit", "estudio-fit", "America/Sao_Paulo");
        tenantRepository.saveAndFlush(tenant);

        assertThat(tenantRepository.findBySlug("estudio-fit")).isPresent();
        assertThat(tenantRepository.existsBySlug("estudio-fit")).isTrue();
        assertThat(tenantRepository.existsBySlug("nao-existe")).isFalse();
    }

    @Test
    void databaseRejectsDuplicatedSlug() {
        tenantRepository.saveAndFlush(Tenant.create("Academia A", "academia-forte", "America/Sao_Paulo"));

        Tenant duplicated = Tenant.create("Academia B", "academia-forte", "America/Sao_Paulo");

        assertThatThrownBy(() -> tenantRepository.saveAndFlush(duplicated))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
