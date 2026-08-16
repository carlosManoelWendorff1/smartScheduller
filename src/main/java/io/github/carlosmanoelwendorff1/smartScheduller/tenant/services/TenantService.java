package io.github.carlosmanoelwendorff1.smartScheduller.tenant.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.exception.TenantNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.exception.TenantSlugAlreadyInUseException;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

/**
 * Orquestra os casos de uso de Tenant. E o unico ponto de entrada esperado
 * para regras de aplicacao envolvendo Tenant (controllers nao devem conter
 * regra de negocio - ver secao 24).
 */
@Service
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant create(String name, String slug, String timezone) {
        // A validacao de formato (nome/slug/timezone) e responsabilidade do
        // proprio agregado Tenant.create(...). Aqui garantimos a regra de
        // negocio que depende de estado externo ao agregado: unicidade do slug.
        Tenant tenant = Tenant.create(name, slug, timezone);

        if (tenantRepository.existsBySlug(tenant.getSlug())) {
            throw new TenantSlugAlreadyInUseException(tenant.getSlug());
        }

        return tenantRepository.save(tenant);
    }

    @Transactional(readOnly = true)
    public Tenant findById(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
    }

    @Transactional(readOnly = true)
    public Page<Tenant> findAll(Pageable pageable) {

        return tenantRepository.findAll(pageable);
    }

    public Tenant rename(UUID tenantId, String newName) {
        Tenant tenant = findById(tenantId);
        tenant.rename(newName);
        return tenant;
    }

    public Tenant changeTimezone(UUID tenantId, String newTimezone) {
        Tenant tenant = findById(tenantId);
        tenant.changeTimezone(newTimezone);
        return tenant;
    }

    public Tenant suspend(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.suspend();
        return tenant;
    }

    public Tenant activate(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.activate();
        return tenant;
    }

    public void close(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.close();
    }
}
