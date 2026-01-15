package com.kh.boot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.Arrays;
import java.util.Map;

@Configuration
public class OpenApiConfig {

        @Value("${kh.openapi.title:KH-Boot API Documentation}")
        private String title;

        @Value("${kh.openapi.description:Backend APIs powered by KH-Boot}")
        private String description;

        @Value("${kh.openapi.version:v1.0.0}")
        private String version;

        @Bean
        @ConditionalOnMissingBean
        public OpenAPI khOpenAPI() {
                return new OpenAPI()
                                .info(new Info().title(title)
                                                .description(description)
                                                .version(version)
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                .externalDocs(new ExternalDocumentation()
                                                .description("Project Wiki")
                                                .url("https://github.com/example/boom"))
                                .addSecurityItem(new SecurityRequirement().addList("bearerToken"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerToken",
                                                                new SecurityScheme()
                                                                                .name("Authorization")
                                                                                .type(SecurityScheme.Type.APIKEY)
                                                                                .in(SecurityScheme.In.HEADER)
                                                                                .description("Enter your token here. Note: Backend now supports tokens with OR without 'Bearer ' prefix.")));
        }

        @Bean
        public GroupedOpenApi systemApi() {
                return GroupedOpenApi.builder()
                                .group("1-System Management (kh-boot)")
                                .packagesToScan("com.kh.boot.controller")
                                .build();
        }

        private static final String[] PUBLIC_ENDPOINTS = {
                        "/login",

                        "/error",
                        "/sms/code",
                        "/email/code",
                        "/login/sms",
                        "/login/email"
        };

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
