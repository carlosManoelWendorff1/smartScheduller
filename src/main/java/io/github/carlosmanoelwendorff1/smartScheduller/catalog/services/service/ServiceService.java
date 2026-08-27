package io.github.carlosmanoelwendorff1.smartScheduller.catalog.services.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.exception.ServiceNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.service.ServiceRepository;

// Yes, "ServiceService" reads a little funny - it's ServiceService the way
// CustomerService is Service<Customer>. Kept for naming consistency with the
// rest of the codebase (entity name + "Service" suffix).
@Service
@Transactional
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service create(
            UUID tenantId, String name, String description, int durationMinutes, BigDecimal price) {
        var service = io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service.create(
                tenantId, name, description, durationMinutes, price);
        return serviceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service findById(
            UUID tenantId, UUID id) {
        return serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ServiceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service> findAll(
            UUID tenantId, Pageable pageable) {
        return serviceRepository.findAllByTenantId(tenantId, pageable);
    }

    public io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service update(
            UUID tenantId, UUID id, String name, String description, int durationMinutes, BigDecimal price) {
        var service = findById(tenantId, id);
        service.update(name, description, durationMinutes, price);
        return service;
    }

    public io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service activate(
            UUID tenantId, UUID id) {
        var service = findById(tenantId, id);
        service.activate();
        return service;
    }

    public io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service deactivate(
            UUID tenantId, UUID id) {
        var service = findById(tenantId, id);
        service.deactivate();
        return service;
    }
}