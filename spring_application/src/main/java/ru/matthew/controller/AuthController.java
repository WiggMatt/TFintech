package ru.matthew.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.matthew.dto.auth.JwtAuthenticationResponse;
import ru.matthew.dto.auth.SignInRequest;
import ru.matthew.dto.auth.SignUpRequest;
import ru.matthew.dto.common.SuccessJsonDTO;
import ru.matthew.service.AuthenticationService;
import ru.matthew.utils.JwtAuthenticationFilter;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрация пользователя", description = "Создает нового пользователя и возвращает JWT токен.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    })
    @PostMapping("/sign-up")
    public JwtAuthenticationResponse registerUser(@RequestBody @Valid SignUpRequest request) {
        log.debug("Регистрация пользователя с email: {}", request.getEmail());
        JwtAuthenticationResponse response = authenticationService.signUp(request);
        log.info("Пользователь с email {} успешно зарегистрирован", request.getEmail());
        return response;
    }

    @Operation(summary = "Авторизация пользователя", description = "Авторизует пользователя и возвращает JWT токен.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно авторизован"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        log.debug("Авторизация пользователя с username: {}", request.getUsername());
        JwtAuthenticationResponse response = authenticationService.signIn(request);
        log.info("Пользователь с username {} успешно авторизован", request.getUsername());
        return response;
    }

    @Operation(summary = "Выход пользователя", description = "Выходит из аккаунта пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно вышел из аккаунта"),
            @ApiResponse(responseCode = "401", description = "Необходимо аутентифицироваться")
    })
    @PostMapping("/sign-out")
    public SuccessJsonDTO signOut(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(JwtAuthenticationFilter.BEARER_PREFIX.length());
        log.debug("Выход пользователя с токеном: {}", token);
        authenticationService.logout(token);
        log.info("Пользователь успешно вышел из аккаунта");
        return new SuccessJsonDTO("Вы успешно вышли из аккаунта");
    }

    @Operation(summary = "Запрос на сброс пароля", description = "Отправляет код для сброса пароля на указанный email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Код для сброса пароля отправлен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping("/reset-password/request")
    public SuccessJsonDTO requestPasswordReset(@RequestParam String email) {
        log.info("Запрос на сброс пароля для email: {}", email);
        authenticationService.sendMessage(email, "Ваш код для сброса пароля: 0000");
        log.info("Код сброса пароля отправлен на email: {}", email);
        return new SuccessJsonDTO("Код для сброса пароля отправлен на указанный адрес");
    }

    @Operation(summary = "Подтверждение сброса пароля", description = "Подтверждает сброс пароля для указанного email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно изменён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректный код сброса пароля")
    })
    @PostMapping("/reset-password/confirm")
    public SuccessJsonDTO confirmPasswordReset(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword) {
        log.info("Подтверждение сброса пароля для email: {}", email);
        authenticationService.updatePassword(email, newPassword, code);
        log.info("Пароль для email {} успешно изменён", email);
        return new SuccessJsonDTO("Пароль успешно изменён");
    }
}
