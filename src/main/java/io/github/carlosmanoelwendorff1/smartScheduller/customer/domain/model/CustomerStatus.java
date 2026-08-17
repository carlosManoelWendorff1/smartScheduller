package io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model;

public enum CustomerStatus {
    /** Customer is active and can be booked/served normally. */
    ACTIVE,
    /**
     * Customer is archived (soft-deleted). Kept for history/CRM timeline purposes.
     */
    INACTIVE
}