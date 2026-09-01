package io.github.carlosmanoelwendorff1.smartScheduller.availability.services;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.AvailabilityCalculator.TimeSlot;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.exception.TenantTimezoneUnavailableException;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.BusyInterval;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.BusyIntervalsPort;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.TenantTimezoneProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ProfessionalAvailabilityRuleService ruleService;

    @Mock
    private BusyIntervalsPort busyIntervalsPort;

    @Mock
    private TenantTimezoneProvider tenantTimezoneProvider;

    private AvailabilityService availabilityService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID professionalId = UUID.randomUUID();
    private final LocalDate date = LocalDate.of(2026, 9, 7); // a Monday
    private final ZoneId zone = ZoneId.of("America/Sao_Paulo");

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(ruleService, busyIntervalsPort, tenantTimezoneProvider);
    }

    @Test
    void returnsEmptyWhenProfessionalHasNoEffectiveAvailability() {
        when(tenantTimezoneProvider.timezoneOf(tenantId)).thenReturn(Optional.of(zone));
        when(ruleService.findEffective(tenantId, professionalId, DayOfWeek.MONDAY)).thenReturn(Optional.empty());

        List<TimeSlot> slots = availabilityService.findAvailableSlots(tenantId, professionalId, null, date, 30, null);

        assertThat(slots).isEmpty();
    }

    @Test
    void returnsEmptyWhenDayIsMarkedClosed() {
        when(tenantTimezoneProvider.timezoneOf(tenantId)).thenReturn(Optional.of(zone));
        when(ruleService.findEffective(tenantId, professionalId, DayOfWeek.MONDAY)).thenReturn(
                Optional.of(new ProfessionalAvailabilityRuleService.EffectiveAvailability(null, null, true,
                        "PROFESSIONAL")));

        List<TimeSlot> slots = availabilityService.findAvailableSlots(tenantId, professionalId, null, date, 30, null);

        assertThat(slots).isEmpty();
    }

    @Test
    void convertsBusyIntervalsFromInstantToLocalTimeUsingTenantTimezone() {
        when(tenantTimezoneProvider.timezoneOf(tenantId)).thenReturn(Optional.of(zone));
        when(ruleService.findEffective(tenantId, professionalId, DayOfWeek.MONDAY)).thenReturn(
                Optional.of(new ProfessionalAvailabilityRuleService.EffectiveAvailability(LocalTime.of(9, 0),
                        LocalTime.of(11, 0), false, "PROFESSIONAL")));

        Instant busyStart = ZonedDateTime.of(date, LocalTime.of(9, 30), zone).toInstant();
        Instant busyEnd = ZonedDateTime.of(date, LocalTime.of(10, 0), zone).toInstant();
        when(busyIntervalsPort.findBusyIntervals(eq(tenantId), eq(professionalId), isNull(), any(), any()))
                .thenReturn(List.of(new BusyInterval(busyStart, busyEnd)));

        List<TimeSlot> slots = availabilityService.findAvailableSlots(tenantId, professionalId, null, date, 30, 30);

        assertThat(slots).contains(new TimeSlot(LocalTime.of(9, 30), false));
        assertThat(slots).contains(new TimeSlot(LocalTime.of(9, 0), true));
    }

    @Test
    void throwsWhenTenantTimezoneCannotBeResolved() {
        when(tenantTimezoneProvider.timezoneOf(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.findAvailableSlots(tenantId, professionalId, null, date, 30, null))
                .isInstanceOf(TenantTimezoneUnavailableException.class);
    }
}