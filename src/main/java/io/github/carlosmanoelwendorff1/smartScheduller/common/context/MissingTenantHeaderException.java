package io.github.carlosmanoelwendorff1.smartScheduller.common.context;

public class MissingTenantHeaderException extends RuntimeException {
    public MissingTenantHeaderException() {
        super("Missing or invalid '" + HeaderTenantContext.TENANT_HEADER + "' header.");
    }
}