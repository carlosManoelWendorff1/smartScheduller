package io.github.carlosmanoelwendorff1.smartScheduller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracao de seguranca minima e temporaria.
 * <p>
 * O modulo de Identity (Fase 1 do roadmap) ainda nao foi implementado, portanto
 * nao
 * existe autenticacao real na plataforma. Esta classe apenas evita que o
 * Spring Security aplique a configuracao padrao (usuario "user" com senha
 * gerada
 * em cada boot), permitindo que os demais modulos sejam desenvolvidos e
 * testados.
 * <p>
 * Ela DEVE ser substituida quando Identity/Authentication forem implementados
 * (autenticacao real, resolucao de tenant a partir do contexto autenticado -
 * ver TenantContext, seção 8 das instrucoes mestre - e autorizacao por
 * Role/Permission - seção 25).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
