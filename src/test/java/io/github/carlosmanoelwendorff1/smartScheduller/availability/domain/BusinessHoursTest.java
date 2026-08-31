// availability/domain/BusinessHoursTest.java
package io.github.carlosmanoelwendorff1.smartScheduller.availability.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.BusinessHours;

class BusinessHoursTest {

    private final UUID tenantId = UUID.randomUUID();

    @Test
    void createsOpenDay() {
        BusinessHours bh = BusinessHours.create(tenantId, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0),
                false);

        assertThat(bh.isClosed()).isFalse();
        assertThat(bh.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void createsClosedDayIgnoringTimes() {
        BusinessHours bh = BusinessHours.create(tenantId, DayOfWeek.SUNDAY, LocalTime.of(9, 0), LocalTime.of(18, 0),
                true);

        assertThat(bh.isClosed()).isTrue();
        assertThat(bh.getStartTime()).isNull();
        assertThat(bh.getEndTime()).isNull();
    }

    @Test
    void rejectsOpenDayWithoutTimes() {
        assertThatThrownBy(() -> BusinessHours.create(tenantId, DayOfWeek.MONDAY, null, null, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThatThrownBy(() -> BusinessHours.create(tenantId, DayOfWeek.MONDAY, LocalTime.of(18, 0),
                LocalTime.of(9, 0), false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateCanFlipToClosed() {
        BusinessHours bh = BusinessHours.create(tenantId, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0),
                false);

        bh.update(null, null, true);

        assertThat(bh.isClosed()).isTrue();
        assertThat(bh.getStartTime()).isNull();
    }
}