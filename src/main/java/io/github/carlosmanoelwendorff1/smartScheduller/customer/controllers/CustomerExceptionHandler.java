package io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.exception.CustomerNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.exception.DuplicateCustomerEmailException;

@RestControllerAdvice(basePackageClasses = CustomerController.class)
public class CustomerExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleNotFound(CustomerNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateCustomerEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateCustomerEmailException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Covers the (rare) case of an invalid tenantId in the URL that doesn't
     * match any row in tenant(id) - caught here as the DB-level foreign key
     * violation, since the customer module doesn't call into tenant's Java
     * classes to pre-validate it (see CustomerService.create).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Invalid or conflicting data (check the tenantId and email).");
    }
}