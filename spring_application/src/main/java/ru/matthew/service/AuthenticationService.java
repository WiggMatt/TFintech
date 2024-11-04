package ru.matthew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import ru.matthew.dao.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.matthew.dto.auth.JwtAuthenticationResponse;
import ru.matthew.dto.auth.SignInRequest;
import ru.matthew.dto.auth.SignUpRequest;
import ru.matthew.dto.common.ErrorJsonDTO;
import ru.matthew.dto.common.SuccessJsonDTO;
import ru.matthew.utils.Role;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse signUp(SignUpRequest request) {

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userService.create(user);

        var jwt = jwtService.generateToken(user, false);
        return new JwtAuthenticationResponse(jwt);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        var jwt = jwtService.generateToken(user, request.getRememberMe());
        return new JwtAuthenticationResponse(jwt);
    }

    public void logout(String token) {
        jwtService.removeToken(token);
    }

    public void updatePassword(String email, String newPassword, String code) {
        if (!"0000".equals(code)) {
            throw new RuntimeException("Неверный код подтверждения");
        }

        if (userService.existsByEmail(email)) {
            User user = userService.getByEmail(email);
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);
        }
    }

    public void sendMessage(String email, String message) {
        // Имитация отправки SMS
        System.out.println("Отправка SMS на " + email + ": " + message);
    }
}