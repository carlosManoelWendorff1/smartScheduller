// identity/security/JwtTenantContext.java
package io.github.carlosmanoelwendorff1.smartScheduller.identity.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;
import io.github.carlosmanoelwendorff1.smartScheduller.exception.TenantContextUnavailableException;

@Component
public class JwtTenantContext implements TenantContext {

    @Override
    public UUID currentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new TenantContextUnavailableException();
        }
        return user.tenantId();
    }
}