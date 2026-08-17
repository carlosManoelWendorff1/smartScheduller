package io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.exception;

import java.util.UUID;

/**
 * Thrown both when the customer truly doesn't exist AND when it exists but
 * belongs to a different tenant. Deliberately the same exception/message for
 * both cases: leaking "it exists, just not for you" would let a caller probe
 * for customer IDs across tenants (never allow cross-tenant access, not even
 * by guessing a UUID in the URL).
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(UUID customerId) {
        super("Customer not found: " + customerId);
    }
}