package ru.matthew.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.lang.Function;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
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
public class JwtService {
    @Value("${token.signing.key}")
    private String jwtSigningKey;

    private final TokenRepository tokenRepository;

    private static final long EXPIRATION_TIME_SHORT = TimeUnit.MINUTES.toMillis(10); // 10 минут
    private static final long EXPIRATION_TIME_LONG = TimeUnit.DAYS.toMillis(30); // 30 дней


    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            claims.put("id", customUserDetails.getId());
            claims.put("email", customUserDetails.getEmail());
            claims.put("role", customUserDetails.getRole());
        }
        String token = generateToken(claims, userDetails, rememberMe);

        saveToken(token,(User) userDetails, rememberMe);

        return token;

    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);

        Optional<Token> savedToken = tokenRepository.findByToken(token);
        return savedToken.isPresent() && !isTokenExpired(token) && userName.equals(userDetails.getUsername());

    }

    public void removeToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);;
    }

    private void saveToken(String token, User user, boolean rememberMe) {
        Instant expirationDate = Instant.now().plusMillis(rememberMe ? EXPIRATION_TIME_LONG : EXPIRATION_TIME_SHORT);

        Token tokenEntity = Token.builder()
                .token(token)
                .expirationDate(expirationDate)
                .user(user)
                .build();

        tokenRepository.save(tokenEntity);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolvers.apply(claims);
        } catch (JwtException e) {
            throw new RuntimeException("Ошибка при разборе токена", e);
        }
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, boolean rememberMe) {
        long expirationTime = rememberMe ? EXPIRATION_TIME_LONG : EXPIRATION_TIME_SHORT;

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}