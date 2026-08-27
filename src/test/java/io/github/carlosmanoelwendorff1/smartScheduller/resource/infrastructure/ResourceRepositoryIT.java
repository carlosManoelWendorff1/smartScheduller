package io.github.carlosmanoelwendorff1.smartScheduller.resource.infrastructure;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.model.resource.Resource;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.repository.resource.ResourceRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

class ResourceRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant createTenant(String slug) {
        return tenantRepository.saveAndFlush(Tenant.create("Tenant " + slug, slug, "America/Sao_Paulo"));
    }

    @Test
    void persistsAndReloadsAResource() {
        Tenant tenant = createTenant("resource-repo-a");
        Resource resource = Resource.create(tenant.getId(), "Cadeira 1", "chair");

        resourceRepository.saveAndFlush(resource);

        Resource reloaded = resourceRepository.findByIdAndTenantId(resource.getId(), tenant.getId()).orElseThrow();
        assertThat(reloaded.getType()).isEqualTo("chair");
    }

    @Test
    void doesNotReturnResourceForAnotherTenant() {
        Tenant tenantA = createTenant("resource-repo-b1");
        Tenant tenantB = createTenant("resource-repo-b2");
        Resource resource = Resource.create(tenantA.getId(), "Cadeira 1", "chair");
        resourceRepository.saveAndFlush(resource);

        assertThat(resourceRepository.findByIdAndTenantId(resource.getId(), tenantB.getId())).isEmpty();
    }

    @Test
    void databaseRejectsResourceForUnknownTenant() {
        Resource resource = Resource.create(UUID.randomUUID(), "Cadeira 1", "chair");

        assertThatThrownBy(() -> resourceRepository.saveAndFlush(resource))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}