package com.familyos.familyos.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI familyosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FamilyOS API")
                        .description("FamilyOS backend API (POC)")
                        .version("0.0.1")
                        .contact(new Contact().name("Shriram")));
    }
}

