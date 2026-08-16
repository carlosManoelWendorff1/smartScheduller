package io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;

/**
 * Repositorio de Tenant.
 * <p>
 * Optamos por usar a interface do Spring Data JPA diretamente como o
 * contrato de dominio, em vez de criar uma interface propria + um adapter
 * de infraestrutura reimplementando o obvio. Isso e coerente com a secao 30
 * das instrucoes mestre ("nao criar abstracoes sem necessidade concreta" /
 * "nao criar generic repositories"): nao ha, hoje, um motivo real para trocar
 * de tecnologia de persistencia, e esta interface ja e um contrato coeso e
 * especifico do agregado Tenant (nao um CrudRepository generico exposto cru).
 */
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
