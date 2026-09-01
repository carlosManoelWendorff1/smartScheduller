package io.github.carlosmanoelwendorff1.smartScheduller.tenant;

import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

public interface TenantTimezoneProvider {
    Optional<ZoneId> timezoneOf(UUID tenantId);
}