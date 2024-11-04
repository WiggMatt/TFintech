package ru.matthew.controller;

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

    @PostMapping("/sign-up")
    public JwtAuthenticationResponse registerUser(@RequestBody @Valid SignUpRequest request) {
        log.debug("Регистрация пользователя с email: {}", request.getEmail());
        JwtAuthenticationResponse response = authenticationService.signUp(request);
        log.info("Пользователь с email {} успешно зарегистрирован", request.getEmail());
        return response;
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        log.debug("Авторизация пользователя с username: {}", request.getUsername());
        JwtAuthenticationResponse response = authenticationService.signIn(request);
        log.info("Пользователь с username {} успешно авторизован", request.getUsername());
        return response;
    }

    @PostMapping("/sign-out")
    public SuccessJsonDTO signOut(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(JwtAuthenticationFilter.BEARER_PREFIX.length());
        log.debug("Выход пользователя с токеном: {}", token);
        authenticationService.logout(token);
        log.info("Пользователь успешно вышел из аккаунта");
        return new SuccessJsonDTO("Вы успешно вышли из аккаунта");
    }

    @PostMapping("/reset-password/request")
    public SuccessJsonDTO requestPasswordReset(@RequestParam String email) {
        log.info("Запрос на сброс пароля для email: {}", email);
        authenticationService.sendMessage(email, "Ваш код для сброса пароля: 0000");
        log.info("Код сброса пароля отправлен на email: {}", email);
        return new SuccessJsonDTO("Код для сброса пароля отправлен на указанный адрес");
    }

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
