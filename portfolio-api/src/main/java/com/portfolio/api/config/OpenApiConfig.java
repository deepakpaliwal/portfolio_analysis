package com.portfolio.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 (Swagger) documentation configuration.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI specification with JWT bearer authentication.
     *
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Portfolio Analysis API")
                .version("1.0.0")
                .description("REST API for Financial Portfolio Analysis Application")
                .contact(new Contact()
                    .name("Portfolio Analysis Team")
                    .email("support@portfolio-analysis.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .schemaRequirement("Bearer", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT authentication token"));
    }
}
