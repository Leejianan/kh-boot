package com.kh.boot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;

import java.util.Arrays;
import java.util.Map;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI boomOpenAPI() {
                return new OpenAPI()
                                .info(new Info().title("Boom API Documentation")
                                                .description("Backend APIs for the Boom Project - Built with Spring Boot 3 & JDK 17")
                                                .version("v1.0.0")
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                .externalDocs(new ExternalDocumentation()
                                                .description("Project Wiki")
                                                .url("https://github.com/example/boom"))
                                // 使用 "token" 作为 SecurityScheme 的标识符，但 header 名称依然是 "Authorization"
                                .addSecurityItem(new SecurityRequirement().addList("bearerToken"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerToken",
                                                                new SecurityScheme()
                                                                                .name("Authorization")
                                                                                .type(SecurityScheme.Type.APIKEY)
                                                                                .in(SecurityScheme.In.HEADER)
                                                                                .description("Enter your token here. Note: Backend now supports tokens with OR without 'Bearer ' prefix.")));
        }

        private static final String[] PUBLIC_ENDPOINTS = { "/login", "/register", "/error" };

        @Bean
        public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
                return openApi -> {
                        if (openApi.getPaths() == null) {
                                return;
                        }

                        openApi.getPaths().entrySet().stream()
                                        .filter(entry -> Arrays.stream(PUBLIC_ENDPOINTS)
                                                        .noneMatch(entry.getKey()::endsWith))
                                        .map(Map.Entry::getValue)
                                        .flatMap(pathItem -> pathItem.readOperations().stream())
                                        .forEach(operation -> operation
                                                        .addSecurityItem(new SecurityRequirement()
                                                                        .addList("bearerToken")));
                };
        }
}
