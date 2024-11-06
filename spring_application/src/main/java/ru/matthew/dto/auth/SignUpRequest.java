package ru.matthew.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос для регистрации нового пользователя")
public class SignUpRequest {

    @Schema(description = "Уникальное имя пользователя",
            example = "user123",
            minLength = 5,
            maxLength = 50)
    @Size(min = 5, max = 50, message = "Имя пользователя должно содержать от 5 до 50 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String username;

    @Schema(description = "Адрес электронной почты пользователя",
            example = "user@example.com",
            minLength = 5,
            maxLength = 255)
    @Size(min = 5, max = 255, message = "Адрес электронной почты должен содержать от 5 до 255 символов")
    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Email адрес должен быть в формате user@example.com")
    private String email;

    @Schema(description = "Пароль пользователя",
            example = "securePassword123",
            maxLength = 255)
    @Size(max = 255, message = "Длина пароля должна быть не более 255 символов")
    private String password;
}
