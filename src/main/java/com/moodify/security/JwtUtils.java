package com.moodify.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${moodify.app.jwtSecret}")
    private String jwtSecretString;

    @Value("${moodify.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Метод для отримання ключа підпису SecretKey
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretString));
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        logger.debug("Generating JWT for user: {}. Expiration: {}", userPrincipal.getUsername(), expiryDate);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        try {
            // Отримуєм Claims (корисне навантаження)
            Claims claims = Jwts.parser()           // Отримуєм будівельник парсера
                    .verifyWith(key())   // Встановлюєм ключ для перевірки підпису
                    .build()             // Будуємо парсер
                    .parseSignedClaims(token) // Парсимо підписаний токен (JWS)
                    .getPayload();       // Отримуєм корисне навантаження (Claims)
            return claims.getSubject(); // Повертаєм ім'я користувача з subject
        } catch (JwtException | IllegalArgumentException e) { // Ловимо помилки парсингу/валідації
            logger.error("Error parsing JWT to get username: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token provided", e);
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()           // Отримуєм будівельник парсера
                    .verifyWith(key())   // Встановлюєм ключ для перевірки підпису
                    .build()             // Будуємо парсер
                    .parseSignedClaims(authToken); // Намагаємось розпарсити. Якщо успішно - винятку не буде.
            return true; // Токен валідний
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT token validation error: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("General JWT exception during validation: {}", e.getMessage());
        }

        return false; // Якщо було спіймано будь-який виняток - токен неваліден
    }
}