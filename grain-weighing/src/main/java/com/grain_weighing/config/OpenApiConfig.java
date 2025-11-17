package com.grain_weighing.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI grainWeighingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Grain Weighing API")
                        .version("v1")
                        .description("API para inserção e relatórios de pesagens de caminhões em balanças ESP32."));
    }
}
