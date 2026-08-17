// customer/domain/CustomerTest.java
package io.github.carlosmanoelwendorff1.smartScheduller.customer.domain;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.CustomerStatus;

class CustomerTest {

    private final UUID tenantId = UUID.randomUUID();

    @Test
    void createsActiveCustomerWithNormalizedEmail() {
        Customer customer = Customer.create(tenantId, "Maria Silva", "Maria.Silva@Example.com",
                "+55 47 99999-0000", "12345678900", LocalDate.of(1990, 5, 20));

        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getTenantId()).isEqualTo(tenantId);
        assertThat(customer.getName()).isEqualTo("Maria Silva");
        assertThat(customer.getEmail()).isEqualTo("maria.silva@example.com");
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(customer.isActive()).isTrue();
    }

    @Test
    void allowsCustomerWithoutEmailOrPhone() {
        Customer customer = Customer.create(tenantId, "Joao Sem Contato", null, null, null, null);

        assertThat(customer.getEmail()).isNull();
        assertThat(customer.getPhone()).isNull();
    }

    @Test
    void rejectsMissingTenantId() {
        assertThatThrownBy(() -> Customer.create(null, "Nome", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Customer.create(tenantId, "  ", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> Customer.create(tenantId, "Nome Valido", "not-an-email", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email");
    }

    @Test
    void rejectsBirthdayInTheFuture() {
        assertThatThrownBy(() -> Customer.create(tenantId, "Nome Valido", null, null, null,
                LocalDate.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void archivedCustomerIsNoLongerActive() {
        Customer customer = Customer.create(tenantId, "Nome Valido", null, null, null, null);

        customer.archive();

        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
        assertThat(customer.isActive()).isFalse();
    }

    @Test
    void updateProfileReplacesContactFields() {
        Customer customer = Customer.create(tenantId, "Nome Valido", "old@example.com", "111", null, null);

        customer.updateProfile("new@example.com", "222", "doc-123", LocalDate.of(2000, 1, 1));

        assertThat(customer.getEmail()).isEqualTo("new@example.com");
        assertThat(customer.getPhone()).isEqualTo("222");
        assertThat(customer.getDocument()).isEqualTo("doc-123");
        assertThat(customer.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
    }
}