package ru.matthew.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.matthew.dao.model.Token;
import ru.matthew.dao.model.User;
import ru.matthew.dao.repository.TokenRepository;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    @Value("${token.signing.key}")
    private String jwtSigningKey;

    private final TokenRepository tokenRepository;

    private static final long EXPIRATION_TIME_SHORT = TimeUnit.MINUTES.toMillis(10);
    private static final long EXPIRATION_TIME_LONG = TimeUnit.DAYS.toMillis(30);

    public String extractUserName(String token) {
        log.debug("Извлечение имени пользователя из токена");
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails, boolean rememberMe) {
        log.info("Генерация JWT токена для пользователя: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof User customUserDetails) {
            claims.put("id", customUserDetails.getId());
            claims.put("email", customUserDetails.getEmail());
            claims.put("role", customUserDetails.getRole());
            log.debug("Добавлены дополнительные клеймы в токен для пользователя {}", customUserDetails.getUsername());
        }

        String token = generateToken(claims, userDetails, rememberMe);
        saveToken(token, (User) userDetails, rememberMe);

        log.info("Токен успешно сгенерирован для пользователя {}", userDetails.getUsername());
        return token;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.debug("Проверка действительности токена для пользователя {}", userDetails.getUsername());

        try {
            final String userName = extractUserName(token);
            Optional<Token> savedToken = tokenRepository.findByToken(token);

            boolean isValid = savedToken.isPresent() && !isTokenExpired(token) && userName.equals(userDetails.getUsername());
            log.info("Результат проверки токена: {}", isValid);
            return isValid;

        } catch (JwtException e) {
            log.error("Ошибка при проверке токена: {}", e.getMessage());
            return false;
        }
    }

    public void removeToken(String token) {
        log.info("Удаление токена из базы данных");
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
        log.debug("Токен успешно удален");
    }

    private void saveToken(String token, User user, boolean rememberMe) {
        Instant expirationDate = Instant.now().plusMillis(rememberMe ? EXPIRATION_TIME_LONG : EXPIRATION_TIME_SHORT);

        Token tokenEntity = Token.builder()
                .token(token)
                .expirationDate(expirationDate)
                .user(user)
                .build();

        tokenRepository.save(tokenEntity);
        log.info("Токен сохранен для пользователя {} с датой истечения {}", user.getUsername(), expirationDate);
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            log.error("Ошибка при извлечении клейма из токена: {}", e.getMessage());
            throw new RuntimeException("Ошибка при разборе токена", e);
        }
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, boolean rememberMe) {
        long expirationTime = rememberMe ? EXPIRATION_TIME_LONG : EXPIRATION_TIME_SHORT;

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(expirationTime)))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        log.debug("Проверка срока действия токена");
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        log.debug("Извлечение всех claims из токена");
        return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        log.debug("Получение ключа для подписи JWT");
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
