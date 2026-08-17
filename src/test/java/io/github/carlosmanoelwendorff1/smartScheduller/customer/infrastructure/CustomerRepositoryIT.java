package io.github.carlosmanoelwendorff1.smartScheduller.customer.infrastructure;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.repository.CustomerRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

class CustomerRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant createTenant(String slug) {
        return tenantRepository.saveAndFlush(Tenant.create("Tenant " + slug, slug, "America/Sao_Paulo"));
    }

    @Test
    void persistsAndReloadsACustomerScopedToItsTenant() {
        Tenant tenant = createTenant("customer-repo-a");
        Customer customer = Customer.create(tenant.getId(), "Maria Silva", "maria@example.com", null, null, null);

        customerRepository.saveAndFlush(customer);

        Customer reloaded = customerRepository.findByIdAndTenantId(customer.getId(), tenant.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Maria Silva");
    }

    @Test
    void doesNotReturnCustomerForAnotherTenant() {
        Tenant tenantA = createTenant("customer-repo-b1");
        Tenant tenantB = createTenant("customer-repo-b2");
        Customer customer = Customer.create(tenantA.getId(), "Maria Silva", null, null, null, null);
        customerRepository.saveAndFlush(customer);

        assertThat(customerRepository.findByIdAndTenantId(customer.getId(), tenantB.getId())).isEmpty();
    }

    @Test
    void databaseRejectsCustomerForUnknownTenant() {
        Customer customer = Customer.create(UUID.randomUUID(), "Maria Silva", null, null, null, null);

        assertThatThrownBy(() -> customerRepository.saveAndFlush(customer))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void enforcesUniqueEmailWithinTheSameTenant() {
        Tenant tenant = createTenant("customer-repo-c");
        customerRepository.saveAndFlush(
                Customer.create(tenant.getId(), "Maria Silva", "duplicate@example.com", null, null, null));

        Customer duplicated = Customer.create(tenant.getId(), "Maria Souza", "duplicate@example.com", null, null,
                null);

        assertThatThrownBy(() -> customerRepository.saveAndFlush(duplicated))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAllByTenantIdIsPaginatedAndScoped() {
        Tenant tenantA = createTenant("customer-repo-d1");
        Tenant tenantB = createTenant("customer-repo-d2");
        customerRepository.saveAndFlush(Customer.create(tenantA.getId(), "Cliente 1", null, null, null, null));
        customerRepository.saveAndFlush(Customer.create(tenantA.getId(), "Cliente 2", null, null, null, null));
        customerRepository.saveAndFlush(Customer.create(tenantB.getId(), "Cliente Outro Tenant", null, null, null,
                null));

        var page = customerRepository.findAllByTenantId(tenantA.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Customer::getTenantId).containsOnly(tenantA.getId());
    }
}