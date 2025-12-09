package com.my.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Конфигурационный класс для настройки OpenAPI документации.
 */
@Configuration
@ComponentScan(basePackages = {"org.springdoc"})
@Import({SpringDocConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        SwaggerConfig.class,
        SwaggerUiConfigProperties.class,
        SwaggerUiOAuthProperties.class,
        JacksonAutoConfiguration.class})
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        Contact contact = new Contact();
        contact.setName("Rodion");
        contact.setEmail("someemail@example");

        License mitLicense = new License().name("GNU AGPLv3")
                .url("https://choosealicenese.com/licesnse/agpl-3.0/");

        Info info = new Info()
                .title("My App API")
                .version("1.0")
                .contact(contact)
                .description("API for My App")
                .termsOfService("http://some.terms.url")
                .license(mitLicense);

        return new OpenAPI().info(info);
    }
}
