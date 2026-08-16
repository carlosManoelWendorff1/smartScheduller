package io.github.carlosmanoelwendorff1.smartScheduller;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base para testes de integracao que precisam de um PostgreSQL real.
 * <p>
 * Sobe um container Postgres efemero por classe de teste (reutilizado entre
 * os metodos da mesma classe) usando Testcontainers. O container e descartado
 * ao final da execucao - nao ha estado compartilhado entre execucoes de teste
 * (ver secao 27 das instrucoes mestre: "Nao utilizar banco H2 como substituto
 * principal do PostgreSQL").
 * <p>
 * {@code @ServiceConnection} injeta automaticamente as propriedades de
 * datasource (url/usuario/senha) no contexto Spring, dispensando configuracao
 * manual em application-test.yml.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Tag("integration")
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"));
}
