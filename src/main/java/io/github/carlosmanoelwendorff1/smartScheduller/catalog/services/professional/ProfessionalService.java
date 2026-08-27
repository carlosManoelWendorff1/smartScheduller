// catalog/services/ProfessionalService.java
package io.github.carlosmanoelwendorff1.smartScheduller.catalog.services.professional;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.exception.ProfessionalNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional.Professional;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.professional.ProfessionalRepository;

@Service
@Transactional
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;

    public ProfessionalService(ProfessionalRepository professionalRepository) {
        this.professionalRepository = professionalRepository;
    }

    public Professional create(UUID tenantId, String name, UUID userId) {
        return professionalRepository.save(Professional.create(tenantId, name, userId));
    }

    @Transactional(readOnly = true)
    public Professional findById(UUID tenantId, UUID id) {
        return professionalRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ProfessionalNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Professional> findAll(UUID tenantId, Pageable pageable) {
        return professionalRepository.findAllByTenantId(tenantId, pageable);
    }

    public Professional rename(UUID tenantId, UUID id, String newName) {
        Professional professional = findById(tenantId, id);
        professional.rename(newName);
        return professional;
    }

    public Professional linkUser(UUID tenantId, UUID id, UUID userId) {
        Professional professional = findById(tenantId, id);
        professional.linkUser(userId);
        return professional;
    }

    public Professional activate(UUID tenantId, UUID id) {
        Professional professional = findById(tenantId, id);
        professional.activate();
        return professional;
    }

    public Professional deactivate(UUID tenantId, UUID id) {
        Professional professional = findById(tenantId, id);
        professional.deactivate();
        return professional;
    }
}