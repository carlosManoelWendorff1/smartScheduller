package io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.exception;

import java.util.UUID;

public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(UUID tenantId) {
        super("Tenant nao encontrado: " + tenantId);
    }
}
