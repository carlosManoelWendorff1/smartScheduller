// resource/domain/ResourceTest.java
package io.github.carlosmanoelwendorff1.smartScheduller.resource.domain;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.model.resource.Resource;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.model.resource.ResourceStatus;

class ResourceTest {

    private final UUID tenantId = UUID.randomUUID();

    @Test
    void createsActiveResource() {
        Resource resource = Resource.create(tenantId, "Cadeira 1", "chair");

        assertThat(resource.getStatus()).isEqualTo(ResourceStatus.ACTIVE);
        assertThat(resource.getType()).isEqualTo("chair");
    }

    @Test
    void rejectsBlankType() {
        assertThatThrownBy(() -> Resource.create(tenantId, "Cadeira 1", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateReplacesNameAndType() {
        Resource resource = Resource.create(tenantId, "Cadeira 1", "chair");

        resource.update("Sala 2", "room");

        assertThat(resource.getName()).isEqualTo("Sala 2");
        assertThat(resource.getType()).isEqualTo("room");
    }

    @Test
    void deactivateThenActivateRoundTrips() {
        Resource resource = Resource.create(tenantId, "Cadeira 1", "chair");

        resource.deactivate();
        assertThat(resource.isActive()).isFalse();

        resource.activate();
        assertThat(resource.isActive()).isTrue();
    }
}