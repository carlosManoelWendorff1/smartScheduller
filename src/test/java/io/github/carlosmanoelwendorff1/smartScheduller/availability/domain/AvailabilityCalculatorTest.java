package io.github.carlosmanoelwendorff1.smartScheduller.availability.domain;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.AvailabilityCalculator.BusyRange;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.AvailabilityCalculator.TimeSlot;

class AvailabilityCalculatorTest {

    @Test
    void generatesAllSlotsAvailableWhenNoBusyRanges() {
        List<TimeSlot> slots = AvailabilityCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(11, 0), 30, 30,
                List.of());

        assertThat(slots).extracting(TimeSlot::time)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30));
        assertThat(slots).allMatch(TimeSlot::available);
    }

    @Test
    void marksSlotUnavailableWhenItOverlapsABusyRange() {
        BusyRange busy = new BusyRange(LocalTime.of(9, 30), LocalTime.of(10, 0));

        List<TimeSlot> slots = AvailabilityCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(10, 30), 30, 30,
                List.of(busy));

        assertThat(slots).containsExactly(
                new TimeSlot(LocalTime.of(9, 0), true),
                new TimeSlot(LocalTime.of(9, 30), false),
                new TimeSlot(LocalTime.of(10, 0), true));
    }

    @Test
    void backToBackBusyRangeDoesNotBlockAdjacentSlot() {
        // Busy 09:00-09:30 exactly; a 09:30 slot should still be free.
        BusyRange busy = new BusyRange(LocalTime.of(9, 0), LocalTime.of(9, 30));

        List<TimeSlot> slots = AvailabilityCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(10, 0), 30, 30,
                List.of(busy));

        assertThat(slots).containsExactly(
                new TimeSlot(LocalTime.of(9, 0), false),
                new TimeSlot(LocalTime.of(9, 30), true));
    }

    @Test
    void doesNotGenerateASlotThatWouldExtendPastTheWindow() {
        // Window is 09:00-09:45, service takes 30min - only one slot fits
        // (09:00-09:30);
        // a candidate at 09:30 would end at 10:00, past the window, so it's excluded.
        List<TimeSlot> slots = AvailabilityCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(9, 45), 30, 30,
                List.of());

        assertThat(slots).containsExactly(new TimeSlot(LocalTime.of(9, 0), true));
    }

    @Test
    void returnsEmptyWhenWindowIsInvalidOrMissing() {
        assertThat(AvailabilityCalculator.calculate(null, LocalTime.of(18, 0), 30, 30, List.of())).isEmpty();
        assertThat(AvailabilityCalculator.calculate(LocalTime.of(18, 0), LocalTime.of(9, 0), 30, 30, List.of()))
                .isEmpty();
    }

    @Test
    void rejectsNonPositiveDuration() {
        assertThatThrownBy(() -> AvailabilityCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(18, 0), 0, 30,
                List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void respectsACustomSlotInterval() {
        List<TimeSlot> slots = AvailabilityCalculator.calculate(LocalTime.of(9, 0), LocalTime.of(10, 0), 15, 15,
                List.of());

        assertThat(slots).extracting(TimeSlot::time)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 15), LocalTime.of(9, 30), LocalTime.of(9, 45));
    }
}