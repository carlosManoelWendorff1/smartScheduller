package io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.exception;

public class AvailabilityRuleNotFoundException extends RuntimeException {
    public AvailabilityRuleNotFoundException(String message) {
        super(message);
    }
}