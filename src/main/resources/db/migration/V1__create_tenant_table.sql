CREATE TABLE tenant
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(150)      NOT NULL,
    slug       VARCHAR(80)       NOT NULL,
    timezone   VARCHAR(50)       NOT NULL,
    status     VARCHAR(20)       NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT uq_tenant_slug UNIQUE (slug)
);

COMMENT ON TABLE tenant IS 'Organizacoes/empresas que utilizam a plataforma (uma barbearia, clinica, oficina, etc).';
COMMENT ON COLUMN tenant.slug IS 'Identificador amigavel e unico usado em URLs/subdominios.';
COMMENT ON COLUMN tenant.timezone IS 'Timezone IANA do tenant (ex: America/Sao_Paulo), usado para interpretar datas de negocio.';
