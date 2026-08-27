package io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.exception;

import java.util.UUID;

public class ProfessionalNotFoundException extends RuntimeException {
    public ProfessionalNotFoundException(UUID id) {
        super("Professional not found: " + id);
    }
}