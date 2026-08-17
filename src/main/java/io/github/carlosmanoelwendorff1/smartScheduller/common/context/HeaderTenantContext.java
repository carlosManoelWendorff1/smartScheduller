// common/context/HeaderTenantContext.java
package io.github.carlosmanoelwendorff1.smartScheduller.common.context;

import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

/**
 * TEMPORARY implementation: resolves the current tenant from an
 * "X-Tenant-Id" header sent by the client.
 * <p>
 * This is NOT real tenant isolation yet - a client can still send whatever
 * tenantId it wants in the header, same as it could before in the URL. What
 * this buys us: every other module depends only on the TenantContext
 * interface, never on how the tenant is resolved. When Identity/auth is
 * implemented (Fase 1), this class gets replaced by one that reads the
 * tenant from the authenticated principal (JWT claim/session) - nothing in
 * customer/catalog/scheduling/etc. needs to change.
 */
@Component
@RequestScope
public class HeaderTenantContext implements TenantContext {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    private final HttpServletRequest request;

    public HeaderTenantContext(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public UUID currentTenantId() {
        String header = request.getHeader(TENANT_HEADER);
        if (header == null || header.isBlank()) {
            throw new MissingTenantHeaderException();
        }
        try {
            return UUID.fromString(header.trim());
        } catch (IllegalArgumentException ex) {
            throw new MissingTenantHeaderException();
        }
    }
}