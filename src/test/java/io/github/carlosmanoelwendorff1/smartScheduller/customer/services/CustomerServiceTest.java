// customer/services/CustomerServiceTest.java
package io.github.carlosmanoelwendorff1.smartScheduller.customer.services;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.exception.CustomerNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.exception.DuplicateCustomerEmailException;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void createsCustomerWhenEmailIsAvailable() {
        when(customerRepository.existsByTenantIdAndEmail(tenantId, "maria@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer customer = customerService.create(tenantId, "Maria", "maria@example.com", null, null, null);

        assertThat(customer.getName()).isEqualTo("Maria");
        verify(customerRepository).save(customer);
    }

    @Test
    void createsCustomerWithoutEmailSkipsUniquenessCheck() {
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customerService.create(tenantId, "Joao", null, null, null, null);

        verify(customerRepository, never()).existsByTenantIdAndEmail(any(), any());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void rejectsCreationWhenEmailAlreadyUsedInTenant() {
        when(customerRepository.existsByTenantIdAndEmail(tenantId, "maria@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(tenantId, "Maria", "maria@example.com", null, null, null))
                .isInstanceOf(DuplicateCustomerEmailException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void findByIdThrowsWhenCustomerBelongsToAnotherTenantOrDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(tenantId, customerId))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void archiveChangesStatusOfExistingCustomer() {
        Customer customer = Customer.create(tenantId, "Maria", null, null, null, null);
        when(customerRepository.findByIdAndTenantId(customer.getId(), tenantId)).thenReturn(Optional.of(customer));

        Customer archived = customerService.archive(tenantId, customer.getId());

        assertThat(archived.isActive()).isFalse();
    }
}