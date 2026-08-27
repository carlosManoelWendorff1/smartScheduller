package io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service.dto.CreateServiceRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service.dto.ServiceResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service.dto.UpdateServiceRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.services.service.ServiceService;
import io.github.carlosmanoelwendorff1.smartScheduller.common.PageResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final TenantContext tenantContext;

    public ServiceController(ServiceService serviceService, TenantContext tenantContext) {
        this.serviceService = serviceService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest request) {
        var service = serviceService.create(tenantContext.currentTenantId(), request.name(), request.description(),
                request.durationMinutes(), request.price());
        ServiceResponse response = ServiceResponse.from(service);
        return ResponseEntity.created(URI.create("/api/v1/services/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<ServiceResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse
                .from(serviceService.findAll(tenantContext.currentTenantId(), pageable).map(ServiceResponse::from));
    }

    @GetMapping("/{id}")
    public ServiceResponse findById(@PathVariable UUID id) {
        return ServiceResponse.from(serviceService.findById(tenantContext.currentTenantId(), id));
    }

    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateServiceRequest request) {
        return ServiceResponse.from(serviceService.update(tenantContext.currentTenantId(), id, request.name(),
                request.description(), request.durationMinutes(), request.price()));
    }

    @PostMapping("/{id}/activate")
    public ServiceResponse activate(@PathVariable UUID id) {
        return ServiceResponse.from(serviceService.activate(tenantContext.currentTenantId(), id));
    }

    @PostMapping("/{id}/deactivate")
    public ServiceResponse deactivate(@PathVariable UUID id) {
        return ServiceResponse.from(serviceService.deactivate(tenantContext.currentTenantId(), id));
    }
}