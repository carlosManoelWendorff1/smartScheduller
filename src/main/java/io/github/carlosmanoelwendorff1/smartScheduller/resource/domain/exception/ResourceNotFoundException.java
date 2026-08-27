package io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(UUID id) {
        super("Resource not found: " + id);
    }
}