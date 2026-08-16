package io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.exception;

/**
 * Lancada quando se tenta criar/atualizar um Tenant com um slug ja utilizado
 * por outro tenant. O slug e um identificador publico e unico (usado em
 * URLs/subdominios), entao esta regra e de negocio, nao apenas uma
 * constraint tecnica do banco.
 */
public class TenantSlugAlreadyInUseException extends RuntimeException {

    public TenantSlugAlreadyInUseException(String slug) {
        super("Ja existe um tenant utilizando o slug '" + slug + "'.");
    }
}
