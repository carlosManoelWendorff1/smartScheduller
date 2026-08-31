package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.exception;

public class AppointmentConflictException extends RuntimeException {
    public AppointmentConflictException(String message) {
        super(message);
    }
}