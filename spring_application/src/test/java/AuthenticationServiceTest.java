import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.matthew.controller.AuthController;
import ru.matthew.dto.auth.JwtAuthenticationResponse;
import ru.matthew.dto.auth.SignInRequest;
import ru.matthew.dto.auth.SignUpRequest;
import ru.matthew.dto.common.SuccessJsonDTO;
import ru.matthew.exception.AuthenticationException;
import ru.matthew.exception.ElementAlreadyExistsException;
import ru.matthew.exception.ElementWasNotFoundException;
import ru.matthew.service.AuthenticationService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationService authenticationService;

    private SignUpRequest signUpRequest;
    private JwtAuthenticationResponse jwtAuthenticationResponse;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest("username", "email@example.com", "password");
        jwtAuthenticationResponse = new JwtAuthenticationResponse("jwt_token");
    }

    @Test
    @DisplayName("Регистрация пользователя - успешный случай")
    void registerUserShouldReturnJwtTokenWhenSuccessful() {
        // Arrange
        when(authenticationService.signUp(signUpRequest)).thenReturn(jwtAuthenticationResponse);

        // Act
        JwtAuthenticationResponse response = authController.registerUser(signUpRequest);

        // Assert
        assertEquals(jwtAuthenticationResponse, response);
        verify(authenticationService).signUp(signUpRequest);
    }

    @Test
    @DisplayName("Регистрация пользователя - пользователь уже существует")
    void registerUserShouldThrowConflictWhenUserExists() {
        // Arrange
        when(authenticationService.signUp(signUpRequest)).thenThrow(new ElementAlreadyExistsException("Пользователь с таким email уже существует"));

        // Act & Assert
        Exception exception = assertThrows(ElementAlreadyExistsException.class, () -> authController.registerUser(signUpRequest));
        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
    }

    @Test
    @DisplayName("Регистрация пользователя - некорректный запрос")
    void registerUserShouldThrowInvalidRequestWhenInvalidRequest() {
        // Arrange
        SignUpRequest invalidRequest = new SignUpRequest("", "invalidemail", ""); // Пустые данные
        when(authenticationService.signUp(invalidRequest)).thenThrow(new AuthenticationException("Некорректный запрос"));

        // Act & Assert
        Exception exception = assertThrows(AuthenticationException.class, () -> authController.registerUser(invalidRequest));
        assertEquals("Некорректный запрос", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideSignInTestArguments")
    @DisplayName("Авторизация пользователя - успешный случай и ошибочный случай")
    void signInShouldReturnJwtTokenOrThrowAuthenticationException(SignInRequest request, JwtAuthenticationResponse expectedResponse, String expectedExceptionMessage) {
        if (expectedResponse != null) {
            when(authenticationService.signIn(request)).thenReturn(expectedResponse);

            // Act
            JwtAuthenticationResponse response = authController.signIn(request);

            // Assert
            assertEquals(expectedResponse, response);
            verify(authenticationService).signIn(request);
        } else {
            when(authenticationService.signIn(request)).thenThrow(new AuthenticationException(expectedExceptionMessage));

            // Act & Assert
            Exception exception = assertThrows(AuthenticationException.class, () -> authController.signIn(request));
            assertEquals(expectedExceptionMessage, exception.getMessage());
        }
    }

    private static Stream<Arguments> provideSignInTestArguments() {
        return Stream.of(
                Arguments.of(new SignInRequest("username", "password", false), new JwtAuthenticationResponse("jwt_token"), null),
                Arguments.of(new SignInRequest("username", "wrongPassword", false), null, "Неверное имя пользователя или пароль"),
                Arguments.of(new SignInRequest("", "", false), null, "Имя пользователя и пароль не могут быть пустыми")
        );
    }

    @Test
    @DisplayName("Выход пользователя - успешный случай")
    void signOutShouldReturnSuccessMessage() {
        // Arrange
        String token = "Bearer jwt_token";
        SuccessJsonDTO expectedResponse = new SuccessJsonDTO("Вы успешно вышли из аккаунта");

        // Act
        SuccessJsonDTO response = authController.signOut(token);

        // Assert
        assertEquals(expectedResponse.getMessage(), response.getMessage());
        verify(authenticationService).logout("jwt_token");
    }

    @ParameterizedTest
    @MethodSource("providePasswordResetArguments")
    @DisplayName("Запрос на сброс пароля и подтверждение сброса пароля")
    void passwordResetShouldReturnSuccessMessageOrThrowException(String email, String code, String newPassword, SuccessJsonDTO expectedResponse, String expectedExceptionMessage) {
        if (expectedResponse != null) {
            SuccessJsonDTO response = authController.confirmPasswordReset(email, code, newPassword);

            // Assert
            assertEquals(expectedResponse.getMessage(), response.getMessage());
            verify(authenticationService).updatePassword(email, newPassword, code);
        } else {
            doThrow(new AuthenticationException(expectedExceptionMessage))
                    .when(authenticationService).updatePassword(email, newPassword, code);

            // Act & Assert
            Exception exception = assertThrows(AuthenticationException.class, () -> authController.confirmPasswordReset(email, code, newPassword));
            assertEquals(expectedExceptionMessage, exception.getMessage());
        }
    }

    private static Stream<Arguments> providePasswordResetArguments() {
        return Stream.of(
                Arguments.of("email@example.com", "0000", "newPassword", new SuccessJsonDTO("Пароль успешно изменён"), null),
                Arguments.of("nonexistent@example.com", "wrongCode", "newPassword", null, "Неверный код сброса пароля")
        );
    }

    @Test
    @DisplayName("Запрос на сброс пароля - успешный случай")
    void requestPasswordResetShouldReturnSuccessMessage() {
        // Arrange
        String email = "email@example.com";
        SuccessJsonDTO expectedResponse = new SuccessJsonDTO("Код для сброса пароля отправлен на указанный адрес");

        // Act
        SuccessJsonDTO response = authController.requestPasswordReset(email);

        // Assert
        assertEquals(expectedResponse.getMessage(), response.getMessage());
        verify(authenticationService).sendMessage(email, "Ваш код для сброса пароля: 0000");
    }

    @Test
    @DisplayName("Запрос на сброс пароля - несуществующий пользователь")
    void requestPasswordResetShouldThrowUserNotFoundException() {
        // Arrange
        String email = "nonexistent@example.com";
        doThrow(new ElementWasNotFoundException("Пользователь с таким email не найден"))
                .when(authenticationService).sendMessage(email, "Ваш код для сброса пароля: 0000");

        // Act & Assert
        Exception exception = assertThrows(ElementWasNotFoundException.class, () -> authController.requestPasswordReset(email));
        assertEquals("Пользователь с таким email не найден", exception.getMessage());
    }
}
