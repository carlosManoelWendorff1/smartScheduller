package io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);
}