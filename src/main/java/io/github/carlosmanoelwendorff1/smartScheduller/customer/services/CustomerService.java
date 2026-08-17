package io.github.carlosmanoelwendorff1.smartScheduller.customer.services;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.exception.CustomerNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.exception.DuplicateCustomerEmailException;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.repository.CustomerRepository;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer create(UUID tenantId, String name, String email, String phone, String document,
            LocalDate birthday) {
        Customer customer = Customer.create(tenantId, name, email, phone, document, birthday);

        if (customer.getEmail() != null
                && customerRepository.existsByTenantIdAndEmail(tenantId, customer.getEmail())) {
            throw new DuplicateCustomerEmailException(customer.getEmail());
        }

        // tenantId itself is backed by a DB-level foreign key to tenant(id)
        // (see V2 migration). We deliberately don't call into the tenant
        // module to check existence here, to avoid a cross-module Java
        // dependency for something the database already guarantees.
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer findById(UUID tenantId, UUID customerId) {
        return customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    @Transactional(readOnly = true)
    public Page<Customer> findAll(UUID tenantId, Pageable pageable) {
        return customerRepository.findAllByTenantId(tenantId, pageable);
    }

    public Customer rename(UUID tenantId, UUID customerId, String newName) {
        Customer customer = findById(tenantId, customerId);
        customer.rename(newName);
        return customer;
    }

    public Customer updateProfile(UUID tenantId, UUID customerId, String email, String phone, String document,
            LocalDate birthday) {
        Customer customer = findById(tenantId, customerId);

        boolean emailChanged = email != null && !email.equalsIgnoreCase(customer.getEmail());
        if (emailChanged && customerRepository.existsByTenantIdAndEmail(tenantId, email.trim().toLowerCase())) {
            throw new DuplicateCustomerEmailException(email);
        }

        customer.updateProfile(email, phone, document, birthday);
        return customer;
    }

    public Customer archive(UUID tenantId, UUID customerId) {
        Customer customer = findById(tenantId, customerId);
        customer.archive();
        return customer;
    }

    public Customer activate(UUID tenantId, UUID customerId) {
        Customer customer = findById(tenantId, customerId);
        customer.activate();
        return customer;
    }
}