package ru.matthew.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    public JwtAuthenticationResponse registerUser(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @PostMapping("/sign-out")
    public SuccessJsonDTO signOut(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(JwtAuthenticationFilter.BEARER_PREFIX.length());
        authenticationService.logout(token);
        return new SuccessJsonDTO("Вы успешно вышли из аккаунта");
    }
}
