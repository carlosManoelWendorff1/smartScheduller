// availability/services/AvailabilityService.java
package io.github.carlosmanoelwendorff1.smartScheduller.availability.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.AvailabilityCalculator;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.AvailabilityCalculator.BusyRange;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.AvailabilityCalculator.TimeSlot;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.exception.TenantTimezoneUnavailableException;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.BusyInterval;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.BusyIntervalsPort;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.TenantTimezoneProvider;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private static final int DEFAULT_SLOT_INTERVAL_MINUTES = 30;

    private final ProfessionalAvailabilityRuleService ruleService;
    private final BusyIntervalsPort busyIntervalsPort;
    private final TenantTimezoneProvider tenantTimezoneProvider;

    public AvailabilityService(ProfessionalAvailabilityRuleService ruleService, BusyIntervalsPort busyIntervalsPort,
            TenantTimezoneProvider tenantTimezoneProvider) {
        this.ruleService = ruleService;
        this.busyIntervalsPort = busyIntervalsPort;
        this.tenantTimezoneProvider = tenantTimezoneProvider;
    }

    public List<TimeSlot> findAvailableSlots(UUID tenantId, UUID professionalId, UUID resourceId, LocalDate date,
            int durationMinutes, Integer slotIntervalMinutes) {
        ZoneId zoneId = tenantTimezoneProvider.timezoneOf(tenantId)
                .orElseThrow(TenantTimezoneUnavailableException::new);

        var effective = ruleService.findEffective(tenantId, professionalId, date.getDayOfWeek());
        if (effective.isEmpty() || effective.get().closed()) {
            return List.of();
        }

        LocalTime windowStart = effective.get().startTime();
        LocalTime windowEnd = effective.get().endTime();

        Instant rangeStart = ZonedDateTime.of(date, windowStart, zoneId).toInstant();
        Instant rangeEnd = ZonedDateTime.of(date, windowEnd, zoneId).toInstant();

        List<BusyInterval> busyIntervals = busyIntervalsPort.findBusyIntervals(tenantId, professionalId, resourceId,
                rangeStart, rangeEnd);

        List<BusyRange> busyRanges = busyIntervals.stream()
                .map(b -> new BusyRange(
                        ZonedDateTime.ofInstant(b.start(), zoneId).toLocalTime(),
                        ZonedDateTime.ofInstant(b.end(), zoneId).toLocalTime()))
                .toList();

        int interval = slotIntervalMinutes != null ? slotIntervalMinutes : DEFAULT_SLOT_INTERVAL_MINUTES;

        return AvailabilityCalculator.calculate(windowStart, windowEnd, durationMinutes, interval, busyRanges);
    }
}