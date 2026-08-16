package io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.TenantStatus;

class TenantTest {

    @Test
    void createsActiveTenantWithNormalizedSlug() {
        Tenant tenant = Tenant.create("Barbearia do Ze", "Barbearia-Do-Ze", "America/Sao_Paulo");

        assertThat(tenant.getId()).isNotNull();
        assertThat(tenant.getName()).isEqualTo("Barbearia do Ze");
        assertThat(tenant.getSlug()).isEqualTo("barbearia-do-ze");
        assertThat(tenant.getTimezone()).isEqualTo("America/Sao_Paulo");
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.isActive()).isTrue();
        assertThat(tenant.getCreatedAt()).isNotNull();
        assertThat(tenant.getUpdatedAt()).isEqualTo(tenant.getCreatedAt());
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Tenant.create("  ", "slug-valido", "America/Sao_Paulo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");
    }

    @ParameterizedTest
    @ValueSource(strings = { "Slug Com Espaco", "slug_com_underscore", "-comeca-com-hifen", "termina-com-hifen-",
            "slug!!" })
    void rejectsInvalidSlug(String invalidSlug) {
        assertThatThrownBy(() -> Tenant.create("Nome Valido", invalidSlug, "America/Sao_Paulo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Slug invalido");
    }

    @Test
    void rejectsInvalidTimezone() {
        assertThatThrownBy(() -> Tenant.create("Nome Valido", "slug-valido", "Nao/Existe"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Timezone invalido");
    }

    @Test
    void suspendedTenantIsNoLongerActive() {
        Tenant tenant = Tenant.create("Clinica X", "clinica-x", "America/Sao_Paulo");

        tenant.suspend();

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
        assertThat(tenant.isActive()).isFalse();
    }

    @Test
    void canReactivateASuspendedTenant() {
        Tenant tenant = Tenant.create("Clinica X", "clinica-x", "America/Sao_Paulo");
        tenant.suspend();

        tenant.activate();

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void closedTenantCannotBeReactivated() {
        Tenant tenant = Tenant.create("Clinica X", "clinica-x", "America/Sao_Paulo");
        tenant.close();

        assertThatThrownBy(tenant::activate)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void closedTenantCannotBeSuspended() {
        Tenant tenant = Tenant.create("Clinica X", "clinica-x", "America/Sao_Paulo");
        tenant.close();

        assertThatThrownBy(tenant::suspend)
                .isInstanceOf(IllegalStateException.class);
    }
}
