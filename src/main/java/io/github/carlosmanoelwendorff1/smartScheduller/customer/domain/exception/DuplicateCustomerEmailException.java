package io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.exception;

public class DuplicateCustomerEmailException extends RuntimeException {
    public DuplicateCustomerEmailException(String email) {
        super("A customer with email '" + email + "' already exists for this tenant.");
    }
}