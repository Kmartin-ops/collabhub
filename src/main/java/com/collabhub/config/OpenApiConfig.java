package com.collabhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI collabHubOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("CollabHub API").description("Developer project and team management platform")
                        .version("v0.2.0").contact(new Contact().name("CollabHub Team").email("dev@collabhub.com"))
                        .license(new License().name("MIT")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://api.collabhub.com").description("Production")));
    }
}
