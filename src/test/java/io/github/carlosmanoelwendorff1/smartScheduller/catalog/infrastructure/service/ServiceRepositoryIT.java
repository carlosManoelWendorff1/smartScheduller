// catalog/infrastructure/ServiceRepositoryIT.java
package io.github.carlosmanoelwendorff1.smartScheduller.catalog.infrastructure.service;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.service.ServiceRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

class ServiceRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant createTenant(String slug) {
        return tenantRepository.saveAndFlush(Tenant.create("Tenant " + slug, slug, "America/Sao_Paulo"));
    }

    @Test
    void persistsAndReloadsAService() {
        Tenant tenant = createTenant("service-repo-a");
        Service service = Service.create(tenant.getId(), "Corte", null, 30, new BigDecimal("50.00"));

        serviceRepository.saveAndFlush(service);

        Service reloaded = serviceRepository.findByIdAndTenantId(service.getId(), tenant.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Corte");
        assertThat(reloaded.getPrice()).isEqualByComparingTo("50.00");
    }

    @Test
    void doesNotReturnServiceForAnotherTenant() {
        Tenant tenantA = createTenant("service-repo-b1");
        Tenant tenantB = createTenant("service-repo-b2");
        Service service = Service.create(tenantA.getId(), "Corte", null, 30, BigDecimal.TEN);
        serviceRepository.saveAndFlush(service);

        assertThat(serviceRepository.findByIdAndTenantId(service.getId(), tenantB.getId())).isEmpty();
    }

    @Test
    void databaseRejectsServiceForUnknownTenant() {
        Service service = Service.create(UUID.randomUUID(), "Corte", null, 30, BigDecimal.TEN);

        assertThatThrownBy(() -> serviceRepository.saveAndFlush(service))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAllByTenantIdIsPaginatedAndScoped() {
        Tenant tenantA = createTenant("service-repo-c1");
        Tenant tenantB = createTenant("service-repo-c2");
        serviceRepository.saveAndFlush(Service.create(tenantA.getId(), "S1", null, 30, BigDecimal.TEN));
        serviceRepository.saveAndFlush(Service.create(tenantA.getId(), "S2", null, 30, BigDecimal.TEN));
        serviceRepository.saveAndFlush(Service.create(tenantB.getId(), "S Outro Tenant", null, 30, BigDecimal.TEN));

        var page = serviceRepository.findAllByTenantId(tenantA.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}