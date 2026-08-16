package io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model;

/**
 * Ciclo de vida de um Tenant na plataforma.
 */
public enum TenantStatus {

    /** Tenant criado e operando normalmente. */
    ACTIVE,

    /**
     * Tenant temporariamente desativado (ex: inadimplencia, suspensao
     * administrativa).
     */
    SUSPENDED,

    /**
     * Tenant encerrado. Dados sao preservados, mas o tenant nao pode mais operar.
     */
    CLOSED
}
