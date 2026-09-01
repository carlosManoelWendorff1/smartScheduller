package io.github.carlosmanoelwendorff1.smartScheduller.scheduling;

import java.time.Instant;

public record BusyInterval(Instant start, Instant end) {
}