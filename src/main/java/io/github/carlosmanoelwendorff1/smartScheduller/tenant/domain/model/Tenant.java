package io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Tenant representa uma organizacao/empresa que utiliza a plataforma
 * (uma barbearia, uma clinica, uma oficina, etc).
 * <p>
 * Todo o isolamento multi-tenant do sistema depende da integridade deste
 * conceito: praticamente toda outra entidade do dominio possuira um
 * {@code tenantId} apontando para este registro (ver secao 7 das
 * instrucoes mestre).
 * <p>
 * O modelo de dominio e anotado diretamente com JPA (abordagem pragmatica -
 * ver secao 30: evitar abstracoes sem necessidade concreta). Caso no futuro
 * seja necessario desacoplar o modelo de persistencia do modelo de dominio,
 * essa decisao devera ser justificada por um requisito real.
 */
@Entity
@Table(name = "tenant")
public class Tenant {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 80, unique = true)
    private String slug;

    @Column(nullable = false, length = 50)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Tenant() {
        // exigido pelo JPA
    }

    private Tenant(UUID id, String name, String slug, String timezone, TenantStatus status, Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.timezone = timezone;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Cria um novo Tenant, aplicando as regras de negocio de criacao
     * (nome obrigatorio, slug valido e timezone IANA valida).
     */
    public static Tenant create(String name, String slug, String timezone) {
        validateName(name);
        String normalizedSlug = validateAndNormalizeSlug(slug);
        validateTimezone(timezone);

        Instant now = Instant.now();
        return new Tenant(UUID.randomUUID(), name.trim(), normalizedSlug, timezone, TenantStatus.ACTIVE, now, now);
    }

    /**
     * Reconstitui um Tenant a partir de dados ja persistidos.
     * Nao reaplica regras de criacao - usado por camadas de mapeamento/testes.
     */
    public static Tenant restore(UUID id, String name, String slug, String timezone, TenantStatus status,
            Instant createdAt, Instant updatedAt) {
        return new Tenant(id, name, slug, timezone, status, createdAt, updatedAt);
    }

    public void rename(String newName) {
        validateName(newName);
        this.name = newName.trim();
        touch();
    }

    public void changeTimezone(String newTimezone) {
        validateTimezone(newTimezone);
        this.timezone = newTimezone;
        touch();
    }

    public void suspend() {
        if (this.status == TenantStatus.CLOSED) {
            throw new IllegalStateException("Nao e possivel suspender um tenant encerrado.");
        }
        this.status = TenantStatus.SUSPENDED;
        touch();
    }

    public void activate() {
        if (this.status == TenantStatus.CLOSED) {
            throw new IllegalStateException("Nao e possivel reativar um tenant encerrado.");
        }
        this.status = TenantStatus.ACTIVE;
        touch();
    }

    public void close() {
        this.status = TenantStatus.CLOSED;
        touch();
    }

    public boolean isActive() {
        return this.status == TenantStatus.ACTIVE;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do tenant e obrigatorio.");
        }
        if (name.trim().length() > 150) {
            throw new IllegalArgumentException("O nome do tenant deve ter no maximo 150 caracteres.");
        }
    }

    private static String validateAndNormalizeSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("O slug do tenant e obrigatorio.");
        }
        String normalized = slug.trim().toLowerCase();
        if (!SLUG_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Slug invalido: use apenas letras minusculas, numeros e hifens (ex: 'barbearia-do-ze').");
        }
        return normalized;
    }

    private static void validateTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("O timezone do tenant e obrigatorio.");
        }
        try {
            ZoneId.of(timezone);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Timezone invalido: " + timezone, ex);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getTimezone() {
        return timezone;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Tenant tenant))
            return false;
        return Objects.equals(id, tenant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
