-- V4__create_service_table.sql
CREATE TABLE service
(
    id               UUID PRIMARY KEY,
    tenant_id        UUID              NOT NULL REFERENCES tenant (id),
    name             VARCHAR(150)      NOT NULL,
    description      VARCHAR(500),
    duration_minutes INTEGER           NOT NULL,
    price            NUMERIC(10, 2)    NOT NULL,
    status           VARCHAR(20)       NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_service_tenant_id ON service (tenant_id);