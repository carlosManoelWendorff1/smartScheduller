package io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers;

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

import io.github.carlosmanoelwendorff1.smartScheduller.common.PageResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers.dto.CreateCustomerRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers.dto.CustomerResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers.dto.RenameCustomerRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers.dto.UpdateCustomerProfileRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.services.CustomerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final TenantContext tenantContext;

    public CustomerController(CustomerService customerService, TenantContext tenantContext) {
        this.customerService = customerService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerService.create(tenantContext.currentTenantId(), request.name(), request.email(),
                request.phone(), request.document(), request.birthday());
        CustomerResponse response = CustomerResponse.from(customer);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<CustomerResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(
                customerService.findAll(tenantContext.currentTenantId(), pageable).map(CustomerResponse::from));
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable UUID id) {
        return CustomerResponse.from(customerService.findById(tenantContext.currentTenantId(), id));
    }

    @PatchMapping("/{id}")
    public CustomerResponse rename(@PathVariable UUID id, @Valid @RequestBody RenameCustomerRequest request) {
        return CustomerResponse.from(customerService.rename(tenantContext.currentTenantId(), id, request.name()));
    }

    @PutMapping("/{id}/profile")
    public CustomerResponse updateProfile(@PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        return CustomerResponse.from(customerService.updateProfile(tenantContext.currentTenantId(), id,
                request.email(), request.phone(), request.document(), request.birthday()));
    }

    @PostMapping("/{id}/archive")
    public CustomerResponse archive(@PathVariable UUID id) {
        return CustomerResponse.from(customerService.archive(tenantContext.currentTenantId(), id));
    }

    @PostMapping("/{id}/activate")
    public CustomerResponse activate(@PathVariable UUID id) {
        return CustomerResponse.from(customerService.activate(tenantContext.currentTenantId(), id));
    }
}