-- V2__create_customer_table.sql
CREATE TABLE customer
(
    id         UUID PRIMARY KEY,
    tenant_id  UUID              NOT NULL REFERENCES tenant (id),
    name       VARCHAR(150)      NOT NULL,
    email      VARCHAR(150),
    phone      VARCHAR(30),
    document   VARCHAR(50),
    birthday   DATE,
    status     VARCHAR(20)       NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT uq_customer_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_customer_tenant_id ON customer (tenant_id);

COMMENT ON TABLE customer IS 'A person who uses a tenant''s services. Generic on purpose: no business-specific fields (see master instructions section 10).';
COMMENT ON COLUMN customer.tenant_id IS 'FK to tenant(id). Enforced at the database level; the customer module does not depend on tenant''s Java classes (Spring Modulith boundary).';