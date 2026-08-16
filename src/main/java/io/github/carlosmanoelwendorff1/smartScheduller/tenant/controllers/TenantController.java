package io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.github.carlosmanoelwendorff1.smartScheduller.common.PageResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers.dto.CreateTenantRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers.dto.RenameTenantRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers.dto.TenantResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.services.TenantService;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints de administracao de Tenants.
 * <p>
 * NOTA: estes endpoints hoje nao sao restritos por autenticacao/autorizacao
 * (ver SecurityConfig) porque o modulo de Identity ainda nao existe. Quando
 * Identity for implementado (Fase 1), este controller passara a exigir um
 * papel administrativo de plataforma (nao um papel dentro de um tenant, ja
 * que a criacao de tenants e uma operacao de administracao da propria
 * plataforma SaaS).
 */
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = tenantService.create(request.name(), request.slug(), request.timezone());
        TenantResponse response = TenantResponse.from(tenant);
        return ResponseEntity.created(URI.create("/api/v1/tenants/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<TenantResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(tenantService.findAll(pageable).map(TenantResponse::from));
    }

    @GetMapping("/{id}")
    public TenantResponse findById(@PathVariable UUID id) {
        return TenantResponse.from(tenantService.findById(id));
    }

    @PatchMapping("/{id}")
    public TenantResponse rename(@PathVariable UUID id, @Valid @RequestBody RenameTenantRequest request) {
        return TenantResponse.from(tenantService.rename(id, request.name()));
    }

    @PutMapping("/{id}/suspend")
    public TenantResponse suspend(@PathVariable UUID id) {
        return TenantResponse.from(tenantService.suspend(id));
    }

    @PutMapping("/{id}/activate")
    public TenantResponse activate(@PathVariable UUID id) {
        return TenantResponse.from(tenantService.activate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void close(@PathVariable UUID id) {
        tenantService.close(id);
    }
}
