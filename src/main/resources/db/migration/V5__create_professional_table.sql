CREATE TABLE professional
(
    id         UUID PRIMARY KEY,
    tenant_id  UUID              NOT NULL REFERENCES tenant (id),
    user_id    UUID              REFERENCES app_user (id),
    name       VARCHAR(150)      NOT NULL,
    status     VARCHAR(20)       NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_professional_tenant_id ON professional (tenant_id);

COMMENT ON COLUMN professional.user_id IS 'Optional link to app_user - a professional may or may not have platform login access.';