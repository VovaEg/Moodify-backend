package com.moodify.security;

import com.moodify.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = null;
        try {
            jwt = parseJwt(request);
            logger.debug("AuthTokenFilter: Parsed JWT: {}", (jwt != null ? jwt.substring(0, Math.min(jwt.length(), 10)) + "..." : "null"));

            if (jwt != null) { // Якщо токен вилучено
                boolean isValid = jwtUtils.validateJwtToken(jwt); // Перевіряємо валідність
                logger.debug("AuthTokenFilter: JWT validation result: {}", isValid);

                if (isValid) { // Якщо токен валіден
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("AuthTokenFilter: Username from JWT: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    // Створюємо об'єкт аутентифікації
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Встановлюєм аутентифікацію в контекст
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("AuthTokenFilter: Set Authentication in SecurityContext for user '{}'", username);
                } else {
                    logger.warn("AuthTokenFilter: Invalid JWT token received.");
                }
            } else {
                logger.debug("AuthTokenFilter: No JWT found in Authorization header.");
            }
        } catch (Exception e) {
            logger.error("AuthTokenFilter: Cannot set user authentication: {}", e.getMessage(), e);
        }

        // Передаємо запит/відповідь далі по цепочці фільтрів
        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает токен из заголовка "Authorization: Bearer <token>".
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Повертаємо частину після "Bearer "
        }
        return null; // Повертаємо null, якщо заголовок не знайдено або невірний
    }
}