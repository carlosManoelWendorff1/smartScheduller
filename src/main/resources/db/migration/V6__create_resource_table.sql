CREATE TABLE resource
(
    id         UUID PRIMARY KEY,
    tenant_id  UUID              NOT NULL REFERENCES tenant (id),
    name       VARCHAR(150)      NOT NULL,
    type       VARCHAR(50)       NOT NULL,
    status     VARCHAR(20)       NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_resource_tenant_id ON resource (tenant_id);

COMMENT ON COLUMN resource.type IS 'Free text (room, chair, equipment, vehicle...) - the system does not know the meaning of a resource type, per master instructions section 13.';