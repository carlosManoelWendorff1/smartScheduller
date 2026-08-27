// resource/controllers/dto/ResourceResponse.java
package io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers.dto;

import java.time.Instant;
import java.util.UUID;

import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.model.resource.Resource;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.model.resource.ResourceStatus;

public record ResourceResponse(UUID id, UUID tenantId, String name, String type, ResourceStatus status,
        Instant createdAt, Instant updatedAt) {
    public static ResourceResponse from(Resource r) {
        return new ResourceResponse(r.getId(), r.getTenantId(), r.getName(), r.getType(), r.getStatus(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}