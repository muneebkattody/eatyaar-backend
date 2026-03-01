// ── SwaggerConfig.java ───────────────────────────────────────────
// Place in com.eatyaar.config

package com.eatyaar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI eatYaarOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("EatYaar API")
                .description("Community food sharing platform API. " +
                    "Use the Authorize button to add your JWT token (Bearer <token>).")
                .version("v1.0")
                .contact(new Contact()
                    .name("EatYaar Team")
                    .email("noreply@eatyaar.in"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://eatyaar.in")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .name("bearerAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}