package io.github.carlosmanoelwendorff1.smartScheduller.tenant.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.exception.TenantNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.exception.TenantSlugAlreadyInUseException;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        tenantService = new TenantService(tenantRepository);
    }

    @Test
    void createsTenantWhenSlugIsAvailable() {
        when(tenantRepository.existsBySlug("barbearia-do-ze")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tenant tenant = tenantService.create("Barbearia do Ze", "barbearia-do-ze", "America/Sao_Paulo");

        assertThat(tenant.getSlug()).isEqualTo("barbearia-do-ze");
        verify(tenantRepository).save(tenant);
    }

    @Test
    void rejectsCreationWhenSlugAlreadyExists() {
        when(tenantRepository.existsBySlug("barbearia-do-ze")).thenReturn(true);

        assertThatThrownBy(() -> tenantService.create("Barbearia do Ze", "barbearia-do-ze", "America/Sao_Paulo"))
                .isInstanceOf(TenantSlugAlreadyInUseException.class);

        verify(tenantRepository, never()).save(any());
    }

    @Test
    void throwsWhenTenantNotFound() {
        UUID id = UUID.randomUUID();
        when(tenantRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.findById(id))
                .isInstanceOf(TenantNotFoundException.class);
    }

    @Test
    void suspendChangesStatusOfExistingTenant() {
        Tenant tenant = Tenant.create("Clinica X", "clinica-x", "America/Sao_Paulo");
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

        Tenant suspended = tenantService.suspend(tenant.getId());

        assertThat(suspended.isActive()).isFalse();
    }

    @Test
    void findAllDelegatesPaginationToRepository() {
        Tenant tenant = Tenant.create("Clinica X", "clinica-x", "America/Sao_Paulo");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Tenant> expectedPage = new PageImpl<>(List.of(tenant), pageable, 1);
        when(tenantRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Tenant> result = tenantService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(tenant);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
