# SmartScheduller — Fase 0 + primeiro fluxo vertical (Tenant)

Bootstrap do modular monolith (Java 21 + Spring Boot 3.3.4 + Maven) com o
primeiro fluxo vertical completo — `model → repository → service →
controller → testes` — implementado sobre o módulo **tenant**, que é a base
de todo o isolamento multi-tenant do sistema.

## Por que Tenant como primeiro fluxo?

Toda outra entidade do domínio (`Customer`, `Service`, `Appointment`, ...)
vai depender de `tenant_id`. Validar a estrutura em camadas
(domain/application/api), migrations, Testcontainers e Spring Modulith em
cima de `Tenant` — que não depende de mais nada — dá uma base sólida para
replicar o mesmo padrão nos próximos módulos (Fase 1: `identity`; Fase 2:
`customer`).

## Estrutura

```
com.smartscheduller
├── config/                 # infra transversal (hoje: SecurityConfig temporário)
└── tenant/                 # módulo Spring Modulith
    ├── domain/
    │   ├── model/           # Tenant (entidade JPA anotada diretamente), TenantStatus
    │   ├── repository/      # TenantRepository (Spring Data JPA)
    │   └── exception/       # exceções de domínio
    ├── application/         # TenantService (casos de uso, transação)
    └── api/                 # TenantController, DTOs, exception handler HTTP
```

> Decisão pragmática: o repositório é a própria interface Spring Data JPA
> dentro de `domain.repository` (sem uma camada extra de adapter em
> `infrastructure`). Não há hoje motivo concreto para trocar de tecnologia
> de persistência — criar essa abstração agora seria complexidade
> antecipada (seção 30 das instruções mestre).

## Rodando localmente

1. Suba o Postgres de desenvolvimento:

   ```bash
   docker compose up -d db
   ```

2. Rode a aplicação com o profile `local`:

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

   O Flyway aplica `V1__create_tenant_table.sql` automaticamente no boot.

3. Teste manualmente:

   ```bash
   curl -X POST http://localhost:8080/api/v1/tenants \
     -H "Content-Type: application/json" \
     -d '{"name":"Barbearia do Zé","slug":"barbearia-do-ze","timezone":"America/Sao_Paulo"}'

   curl http://localhost:8080/api/v1/tenants
   curl http://localhost:8080/actuator/health
   ```

## Rodando os testes

```bash
mvn test
```

Os testes de integração (`*IT.java` e o teste de contexto) sobem um
Postgres **efêmero** via Testcontainers automaticamente — não é necessário
ter o `docker compose up` rodando para rodar `mvn test`, só é necessário
Docker disponível na máquina/CI.

Cobertura incluída:

- `TenantTest` — regras do agregado (validação de nome/slug/timezone,
  transições de estado ACTIVE/SUSPENDED/CLOSED) — unitário, sem Spring.
- `TenantServiceTest` — regra de unicidade de slug e orquestração dos casos
  de uso — unitário, com `TenantRepository` mockado (Mockito).
- `TenantRepositoryIT` — mapeamento JPA + migration + constraint de
  unicidade no banco — integração, Postgres real via Testcontainers.
- `TenantControllerIT` — fluxo HTTP completo (criar, buscar, listar,
  renomear, suspender, ativar, encerrar; validação; 404; 409 de slug
  duplicado) — integração, `MockMvc` + Postgres real.
- `SmartSchedullerApplicationTests` — o contexto Spring sobe corretamente.
- `ModularityTests` — Spring Modulith valida os boundaries dos módulos e
  gera a documentação de arquitetura (`target/spring-modulith-docs`).

O `docker-compose.yml` também expõe um serviço opcional `db-test`
(`docker compose --profile test up -d db-test`) só para inspeção manual —
os testes JUnit não dependem dele.

## O que falta (próximas fases, não incluído aqui de propósito)

- Módulo `identity` (User, autenticação real) — hoje `SecurityConfig`
  libera todas as rotas porque ainda não existe autenticação. Isso **deve**
  ser substituído na Fase 1.
- `TenantContext` (seção 8) — depende de Identity para resolver o tenant a
  partir do usuário autenticado; por isso ainda não foi criado.
- Demais módulos (`customer`, `catalog`, `resource`, `scheduling`, ...)
  seguirão a mesma estrutura em camadas usada aqui.

## Observação importante

Este ambiente não tem acesso à internet nem Maven instalado, então não foi
possível rodar `mvn compile`/`mvn test` para validar o build de ponta a
ponta antes da entrega. O código foi escrito e revisado com cuidado, mas
recomendo rodar `mvn test` como primeiro passo ao importar o projeto — se
algo não compilar (typo, versão de dependência), me avise com o erro que eu
corrijo.
