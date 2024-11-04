package ru.matthew.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.matthew.dao.model.User;
import ru.matthew.dto.auth.JwtAuthenticationResponse;
import ru.matthew.dto.auth.SignInRequest;
import ru.matthew.dto.auth.SignUpRequest;
import ru.matthew.exception.AuthenticationException;
import ru.matthew.exception.ElementAlreadyExistsException;
import ru.matthew.utils.Role;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        log.info("Начало регистрации пользователя с email: {}", request.getEmail());

        if (userService.existsByEmail(request.getEmail())) {
            log.warn("Пользователь с email {} уже существует", request.getEmail());
            throw new ElementAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user = createUser(request);
        userService.create(user);

        log.info("Пользователь с email {} успешно зарегистрирован", request.getEmail());
        return generateJwtResponse(user, false);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        log.info("Попытка аутентификации пользователя с email: {}", request.getUsername());
        authenticateUser(request);

        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(request.getUsername());

        User user = userService.getByEmail(userDetails.getUsername());

        log.info("Пользователь с email {} успешно аутентифицирован", request.getUsername());

        return generateJwtResponse(user, request.getRememberMe());
    }

    public void logout(String token) {
        log.info("Попытка выхода пользователя с токеном");
        jwtService.removeToken(token);
        log.info("Пользователь успешно вышел из аккаунта");
    }

    public void updatePassword(String email, String newPassword, String code) {
        log.info("Запрос на изменение пароля для email: {}", email);

        validateResetCode(code);
        User user = userService.getByEmail(email);

        updatePasswordForUser(user, newPassword);
        log.info("Пароль для пользователя с email {} успешно обновлен", email);
    }

    public void sendMessage(String email, String message) {
        log.info("Имитация отправки SMS на email {} с сообщением: {}", email, message);
    }


    private User createUser(SignUpRequest request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
    }

    private JwtAuthenticationResponse generateJwtResponse(User user, boolean rememberMe) {
        String jwt = jwtService.generateToken(user, rememberMe);
        log.debug("Токен JWT сгенерирован для пользователя с email {}", user.getEmail());
        return new JwtAuthenticationResponse(jwt);
    }

    private void authenticateUser(SignInRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            log.error("Ошибка аутентификации для пользователя {}: {}", request.getUsername(), e.getMessage());
            throw new AuthenticationException("Неверное имя пользователя или пароль");
        }
    }

    private void validateResetCode(String code) {
        if (!"0000".equals(code)) {
            log.warn("Неверный код подтверждения: {}", code);
            throw new AuthenticationException("Неверный код подтверждения");
        }
    }

    private void updatePasswordForUser(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
    }
}