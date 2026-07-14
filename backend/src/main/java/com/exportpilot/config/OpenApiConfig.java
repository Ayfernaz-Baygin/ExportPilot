package com.exportpilot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI exportPilotOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ExportPilot API")
                        .version("v1")
                        .description(
                                "AI-supported export market analysis and "
                                        + "decision support platform API."
                        )
                        .contact(new Contact()
                                .name("ExportPilot Development Team"))
                        .license(new License()
                                .name("Proprietary")));
    }
}