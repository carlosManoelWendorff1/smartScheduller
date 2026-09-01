package io.github.carlosmanoelwendorff1.smartScheduller.scheduling;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BusyIntervalsPort {
    List<BusyInterval> findBusyIntervals(UUID tenantId, UUID professionalId, UUID resourceId, Instant rangeStart,
            Instant rangeEnd);
}