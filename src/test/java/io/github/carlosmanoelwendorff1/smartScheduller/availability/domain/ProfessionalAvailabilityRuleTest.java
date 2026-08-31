package io.github.carlosmanoelwendorff1.smartScheduller.availability.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.ProfessionalAvailabilityRule;

class ProfessionalAvailabilityRuleTest {

    private final UUID tenantId = UUID.randomUUID();
    private final UUID professionalId = UUID.randomUUID();

    @Test
    void createsOpenDayRule() {
        var rule = ProfessionalAvailabilityRule.create(tenantId, professionalId, DayOfWeek.TUESDAY,
                LocalTime.of(8, 0), LocalTime.of(17, 0), false);

        assertThat(rule.isClosed()).isFalse();
        assertThat(rule.getProfessionalId()).isEqualTo(professionalId);
    }

    @Test
    void rejectsMissingProfessionalId() {
        assertThatThrownBy(() -> ProfessionalAvailabilityRule.create(tenantId, null, DayOfWeek.TUESDAY,
                LocalTime.of(8, 0), LocalTime.of(17, 0), false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("professionalId");
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThatThrownBy(() -> ProfessionalAvailabilityRule.create(tenantId, professionalId, DayOfWeek.TUESDAY,
                LocalTime.of(17, 0), LocalTime.of(8, 0), false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}