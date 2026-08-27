package io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.exception;

import java.util.UUID;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(UUID id) {
        super("Service not found: " + id);
    }
}