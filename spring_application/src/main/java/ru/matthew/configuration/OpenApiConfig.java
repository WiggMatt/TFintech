package ru.matthew.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "T-Fintech",
                version = "1.0.0",
                contact = @Contact(
                        name = "Конюхов Матвей",
                        email = "konyukhov.matt@yandex.ru"
                )
        )
)
public class OpenApiConfig {
    // Конфигурация для Swagger
}