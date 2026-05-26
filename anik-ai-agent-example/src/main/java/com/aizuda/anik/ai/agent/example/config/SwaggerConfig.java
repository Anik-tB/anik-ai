package com.aizuda.anik.ai.agent.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger configuration
 *
 * @author openanik
 * @date 2026-04-25
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Anik AI OpenAPI Client Demo")
                        .version("1.0.0")
                        .description("Demonstrate how to use Anik AI OpenAPI Client to call the server interface")
                        .contact(new Contact()
                                .name("OpenAnik")
                                .url("https://github.com/aizuda/anik-ai"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
