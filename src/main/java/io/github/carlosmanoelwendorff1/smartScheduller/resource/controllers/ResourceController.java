package io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers;

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

import io.github.carlosmanoelwendorff1.smartScheduller.common.PageResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers.dto.CreateResourceRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers.dto.ResourceResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers.dto.UpdateResourceRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.services.ResourceService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final TenantContext tenantContext;

    public ResourceController(ResourceService resourceService, TenantContext tenantContext) {
        this.resourceService = resourceService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<ResourceResponse> create(@Valid @RequestBody CreateResourceRequest request) {
        var resource = resourceService.create(tenantContext.currentTenantId(), request.name(), request.type());
        ResourceResponse response = ResourceResponse.from(resource);
        return ResponseEntity.created(URI.create("/api/v1/resources/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<ResourceResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse
                .from(resourceService.findAll(tenantContext.currentTenantId(), pageable).map(ResourceResponse::from));
    }

    @GetMapping("/{id}")
    public ResourceResponse findById(@PathVariable UUID id) {
        return ResourceResponse.from(resourceService.findById(tenantContext.currentTenantId(), id));
    }

    @PutMapping("/{id}")
    public ResourceResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateResourceRequest request) {
        return ResourceResponse
                .from(resourceService.update(tenantContext.currentTenantId(), id, request.name(), request.type()));
    }

    @PostMapping("/{id}/activate")
    public ResourceResponse activate(@PathVariable UUID id) {
        return ResourceResponse.from(resourceService.activate(tenantContext.currentTenantId(), id));
    }

    @PostMapping("/{id}/deactivate")
    public ResourceResponse deactivate(@PathVariable UUID id) {
        return ResourceResponse.from(resourceService.deactivate(tenantContext.currentTenantId(), id));
    }
}