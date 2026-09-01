package io.github.carlosmanoelwendorff1.smartScheduller.tenant.services;

import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.tenant.TenantTimezoneProvider;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

@Component
public class JpaTenantTimezoneProvider implements TenantTimezoneProvider {

    private final TenantRepository tenantRepository;

    public JpaTenantTimezoneProvider(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ZoneId> timezoneOf(UUID tenantId) {
        return tenantRepository.findById(tenantId).map(t -> ZoneId.of(t.getTimezone()));
    }
}