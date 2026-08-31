CREATE TABLE business_hours
(
    id         UUID PRIMARY KEY,
    tenant_id  UUID              NOT NULL REFERENCES tenant (id),
    day_of_week VARCHAR(10)      NOT NULL,
    start_time TIME,
    end_time   TIME,
    closed     BOOLEAN           NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT uq_business_hours_tenant_day UNIQUE (tenant_id, day_of_week),
    CONSTRAINT chk_business_hours_period CHECK (
        closed = TRUE OR (start_time IS NOT NULL AND end_time IS NOT NULL AND end_time > start_time)
    )
);

CREATE INDEX idx_business_hours_tenant_id ON business_hours (tenant_id);

COMMENT ON TABLE business_hours IS 'Tenant-wide default weekly schedule, one row per day of week. Acts as a fallback for professionals without their own rule for that day.';