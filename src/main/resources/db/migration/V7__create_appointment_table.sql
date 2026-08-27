CREATE TABLE appointment
(
    id              UUID PRIMARY KEY,
    tenant_id       UUID              NOT NULL REFERENCES tenant (id),
    customer_id     UUID              NOT NULL REFERENCES customer (id),
    service_id      UUID              NOT NULL REFERENCES service (id),
    professional_id UUID              REFERENCES professional (id),
    resource_id     UUID              REFERENCES resource (id),
    start_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status          VARCHAR(20)       NOT NULL,
    notes           VARCHAR(1000),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT chk_appointment_end_after_start CHECK (end_at > start_at)
);

CREATE INDEX idx_appointment_tenant_id ON appointment (tenant_id);
CREATE INDEX idx_appointment_customer_id ON appointment (customer_id);
CREATE INDEX idx_appointment_professional_id ON appointment (professional_id);
CREATE INDEX idx_appointment_resource_id ON appointment (resource_id);
CREATE INDEX idx_appointment_start_at ON appointment (start_at);