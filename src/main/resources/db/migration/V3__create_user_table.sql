-- V3__create_user_table.sql
CREATE TABLE app_user
(
    id            UUID PRIMARY KEY,
    tenant_id     UUID              NOT NULL REFERENCES tenant (id),
    name          VARCHAR(150)      NOT NULL,
    email         VARCHAR(150)      NOT NULL,
    password_hash VARCHAR(100)      NOT NULL,
    role          VARCHAR(20)       NOT NULL,
    status        VARCHAR(20)       NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT uq_app_user_email UNIQUE (email)
);

CREATE INDEX idx_app_user_tenant_id ON app_user (tenant_id);

COMMENT ON TABLE app_user IS 'Staff account belonging to a tenant. Table is "app_user", not "user", because USER is a reserved word in Postgres.';