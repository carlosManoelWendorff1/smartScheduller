// resource/services/ResourceService.java
package io.github.carlosmanoelwendorff1.smartScheduller.resource.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.exception.ResourceNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.model.resource.Resource;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.repository.resource.ResourceRepository;

@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Resource create(UUID tenantId, String name, String type) {
        return resourceRepository.save(Resource.create(tenantId, name, type));
    }

    @Transactional(readOnly = true)
    public Resource findById(UUID tenantId, UUID id) {
        return resourceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Resource> findAll(UUID tenantId, Pageable pageable) {
        return resourceRepository.findAllByTenantId(tenantId, pageable);
    }

    public Resource update(UUID tenantId, UUID id, String name, String type) {
        Resource resource = findById(tenantId, id);
        resource.update(name, type);
        return resource;
    }

    public Resource activate(UUID tenantId, UUID id) {
        Resource resource = findById(tenantId, id);
        resource.activate();
        return resource;
    }

    public Resource deactivate(UUID tenantId, UUID id) {
        Resource resource = findById(tenantId, id);
        resource.deactivate();
        return resource;
    }
}