-- V10__create_professional_availability_rule_table.sql
CREATE TABLE professional_availability_rule
(
    id              UUID PRIMARY KEY,
    tenant_id       UUID              NOT NULL,
    professional_id UUID              NOT NULL,
    day_of_week     VARCHAR(10)       NOT NULL,
    start_time      TIME,
    end_time        TIME,
    closed          BOOLEAN           NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT uq_prof_availability_prof_day UNIQUE (professional_id, day_of_week),
    CONSTRAINT chk_prof_availability_period CHECK (
        closed = TRUE OR (start_time IS NOT NULL AND end_time IS NOT NULL AND end_time > start_time)
    ),
    CONSTRAINT fk_prof_availability_professional_tenant
        FOREIGN KEY (professional_id, tenant_id) REFERENCES professional (id, tenant_id)
);

CREATE INDEX idx_prof_availability_tenant_id ON professional_availability_rule (tenant_id);
CREATE INDEX idx_prof_availability_professional_id ON professional_availability_rule (professional_id);

COMMENT ON CONSTRAINT fk_prof_availability_professional_tenant ON professional_availability_rule IS 'Composite FK: guarantees professional_id belongs to tenant_id at the database level, not just that it exists.';