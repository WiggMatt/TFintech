package ru.matthew.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос для входа пользователя в систему")
public class SignInRequest {

    @Schema(description = "Имя пользователя",
            example = "user123",
            minLength = 5,
            maxLength = 50)
    @Size(min = 5, max = 50, message = "Имя пользователя должно содержать от 5 до 50 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String username;

    @Schema(description = "Пароль для авторизации",
            example = "securePassword123",
            minLength = 8,
            maxLength = 255)
    @Size(min = 8, max = 255, message = "Длина пароля должна быть от 8 до 255 символов")
    @NotBlank(message = "Пароль не может быть пустыми")
    private String password;

    @Schema(description = "Флаг запоминания пользователя",
            example = "true",
            defaultValue = "false")
    @NotNull
    private Boolean rememberMe = false;
}
