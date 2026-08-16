package io.github.carlosmanoelwendorff1.smartScheduller.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartSchedullerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartScheduller API")
                        .description("Multi-tenant scheduling/CRM platform API.")
                        .version("v0.1"));
    }
}