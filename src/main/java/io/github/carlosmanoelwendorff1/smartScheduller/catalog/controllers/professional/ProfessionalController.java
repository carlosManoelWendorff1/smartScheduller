// catalog/controllers/ProfessionalController.java
package io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.dto.CreateProfessionalRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.dto.LinkUserRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.dto.ProfessionalResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.dto.RenameProfessionalRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.services.professional.ProfessionalService;
import io.github.carlosmanoelwendorff1.smartScheduller.common.PageResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;
    private final TenantContext tenantContext;

    public ProfessionalController(ProfessionalService professionalService, TenantContext tenantContext) {
        this.professionalService = professionalService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<ProfessionalResponse> create(@Valid @RequestBody CreateProfessionalRequest request) {
        var professional = professionalService.create(tenantContext.currentTenantId(), request.name(),
                request.userId());
        ProfessionalResponse response = ProfessionalResponse.from(professional);
        return ResponseEntity.created(URI.create("/api/v1/professionals/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<ProfessionalResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(
                professionalService.findAll(tenantContext.currentTenantId(), pageable).map(ProfessionalResponse::from));
    }

    @GetMapping("/{id}")
    public ProfessionalResponse findById(@PathVariable UUID id) {
        return ProfessionalResponse.from(professionalService.findById(tenantContext.currentTenantId(), id));
    }

    @PatchMapping("/{id}")
    public ProfessionalResponse rename(@PathVariable UUID id, @Valid @RequestBody RenameProfessionalRequest request) {
        return ProfessionalResponse
                .from(professionalService.rename(tenantContext.currentTenantId(), id, request.name()));
    }

    @PutMapping("/{id}/user")
    public ProfessionalResponse linkUser(@PathVariable UUID id, @RequestBody LinkUserRequest request) {
        return ProfessionalResponse
                .from(professionalService.linkUser(tenantContext.currentTenantId(), id, request.userId()));
    }

    @PostMapping("/{id}/activate")
    public ProfessionalResponse activate(@PathVariable UUID id) {
        return ProfessionalResponse.from(professionalService.activate(tenantContext.currentTenantId(), id));
    }

    @PostMapping("/{id}/deactivate")
    public ProfessionalResponse deactivate(@PathVariable UUID id) {
        return ProfessionalResponse.from(professionalService.deactivate(tenantContext.currentTenantId(), id));
    }
}