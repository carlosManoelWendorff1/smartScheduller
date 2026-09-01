package io.github.carlosmanoelwendorff1.smartScheduller.availability.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure interval math - no Spring, no database, no timezone. Business hours
 * and busy intervals both come in already resolved to LocalTime for a
 * single day; the caller (AvailabilityService) handles timezone conversion.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {
    }

    public record BusyRange(LocalTime start, LocalTime end) {
    }

    public record TimeSlot(LocalTime time, boolean available) {
    }

    public static List<TimeSlot> calculate(LocalTime windowStart, LocalTime windowEnd, int durationMinutes,
            int slotIntervalMinutes, List<BusyRange> busyRanges) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be greater than zero.");
        }
        if (slotIntervalMinutes <= 0) {
            throw new IllegalArgumentException("slotIntervalMinutes must be greater than zero.");
        }
        if (windowStart == null || windowEnd == null || !windowEnd.isAfter(windowStart)) {
            return List.of();
        }

        List<TimeSlot> slots = new ArrayList<>();
        LocalTime candidate = windowStart;

        while (true) {
            LocalTime candidateEnd = candidate.plusMinutes(durationMinutes);
            // plusMinutes wraps past midnight instead of throwing - bail out
            // once the full duration no longer fits before the window closes.
            if (candidateEnd.isBefore(candidate) || candidateEnd.isAfter(windowEnd)) {
                break;
            }

            LocalTime slotStart = candidate;
            LocalTime slotEnd = candidateEnd;
            boolean available = busyRanges.stream()
                    .noneMatch(b -> overlaps(slotStart, slotEnd, b.start(), b.end()));
            slots.add(new TimeSlot(slotStart, available));

            LocalTime next = candidate.plusMinutes(slotIntervalMinutes);
            if (!next.isAfter(candidate)) {
                break; // wrapped past midnight
            }
            candidate = next;
        }

        return slots;
    }

    private static boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }
}