package io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.professional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional.Professional;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional.ProfessionalStatus;

class ProfessionalTest {

    private final UUID tenantId = UUID.randomUUID();

    @Test
    void createsActiveProfessionalWithoutUserLink() {
        Professional professional = Professional.create(tenantId, "Dra. Ana", null);

        assertThat(professional.getStatus()).isEqualTo(ProfessionalStatus.ACTIVE);
        assertThat(professional.getUserId()).isNull();
    }

    @Test
    void createsProfessionalLinkedToAUser() {
        UUID userId = UUID.randomUUID();
        Professional professional = Professional.create(tenantId, "Dr. Joao", userId);

        assertThat(professional.getUserId()).isEqualTo(userId);
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Professional.create(tenantId, " ", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void linkAndUnlinkUser() {
        Professional professional = Professional.create(tenantId, "Dra. Ana", null);
        UUID userId = UUID.randomUUID();

        professional.linkUser(userId);
        assertThat(professional.getUserId()).isEqualTo(userId);

        professional.unlinkUser();
        assertThat(professional.getUserId()).isNull();
    }

    @Test
    void deactivateThenActivateRoundTrips() {
        Professional professional = Professional.create(tenantId, "Dra. Ana", null);

        professional.deactivate();
        assertThat(professional.isActive()).isFalse();

        professional.activate();
        assertThat(professional.isActive()).isTrue();
    }
}