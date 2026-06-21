package com.orchestra.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI orchestraOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orchestra API")
                        .description("Prototype backend for BPMN, sequence, and OpenAPI-driven test scenario generation.")
                        .version("v1.0.0"));
    }
}
