package io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.service;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.ServiceStatus;

class ServiceTest {

    private final UUID tenantId = UUID.randomUUID();

    @Test
    void createsActiveService() {
        Service service = Service.create(tenantId, "Corte de Cabelo", "Corte tradicional", 30,
                new BigDecimal("50.00"));

        assertThat(service.getStatus()).isEqualTo(ServiceStatus.ACTIVE);
        assertThat(service.getDurationMinutes()).isEqualTo(30);
        assertThat(service.isActive()).isTrue();
    }

    @Test
    void rejectsZeroOrNegativeDuration() {
        assertThatThrownBy(() -> Service.create(tenantId, "Nome", null, 0, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativePrice() {
        assertThatThrownBy(() -> Service.create(tenantId, "Nome", null, 30, new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allowsZeroPrice() {
        Service service = Service.create(tenantId, "Cortesia", null, 15, BigDecimal.ZERO);

        assertThat(service.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void deactivateThenActivateRoundTrips() {
        Service service = Service.create(tenantId, "Nome", null, 30, BigDecimal.TEN);

        service.deactivate();
        assertThat(service.isActive()).isFalse();

        service.activate();
        assertThat(service.isActive()).isTrue();
    }

    @Test
    void updateReplacesFields() {
        Service service = Service.create(tenantId, "Nome Antigo", "desc antiga", 30, BigDecimal.TEN);

        service.update("Nome Novo", "desc nova", 45, new BigDecimal("99.90"));

        assertThat(service.getName()).isEqualTo("Nome Novo");
        assertThat(service.getDurationMinutes()).isEqualTo(45);
        assertThat(service.getPrice()).isEqualByComparingTo("99.90");
    }
}