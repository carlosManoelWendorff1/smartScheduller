package io.github.carlosmanoelwendorff1.smartScheduller.exception;

public class TenantContextUnavailableException extends RuntimeException {
    public TenantContextUnavailableException() {
        super("No authenticated tenant context available.");
    }
}