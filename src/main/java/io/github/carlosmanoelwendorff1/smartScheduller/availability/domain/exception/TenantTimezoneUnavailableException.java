package io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.exception;

public class TenantTimezoneUnavailableException extends RuntimeException {
    public TenantTimezoneUnavailableException() {
        super("Could not resolve the tenant's timezone.");
    }
}