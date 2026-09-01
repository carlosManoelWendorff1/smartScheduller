package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.BusyInterval;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.BusyIntervalsPort;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.AppointmentStatus;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.repository.AppointmentRepository;

/**
 * Reuses the same findOverlapping query built for conflict-checking on
 * create/reschedule - "busy intervals" and "conflicting appointments" are
 * the same underlying data, just consumed by a different caller.
 */
@Component
public class AppointmentBusyIntervalsAdapter implements BusyIntervalsPort {

    private static final List<AppointmentStatus> ACTIVE_STATUSES = List.of(AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;

    public AppointmentBusyIntervalsAdapter(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusyInterval> findBusyIntervals(UUID tenantId, UUID professionalId, UUID resourceId,
            Instant rangeStart, Instant rangeEnd) {
        // excludeId: nothing to exclude here (this isn't a reschedule check
        // against "itself"), so pass a fresh UUID that can never match a real row.
        return appointmentRepository
                .findOverlapping(tenantId, UUID.randomUUID(), professionalId, resourceId, rangeStart, rangeEnd,
                        ACTIVE_STATUSES)
                .stream()
                .map(a -> new BusyInterval(a.getStartAt(), a.getEndAt()))
                .toList();
    }
}