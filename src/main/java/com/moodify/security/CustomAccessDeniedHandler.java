package com.moodify.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component // Помічаемо як компонент Spring
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // Логуємо спробу неавторизованого доступу
        logger.warn("Access Denied for user trying to access {}: {}", request.getRequestURI(), accessDeniedException.getMessage());

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: You do not have the required permissions to access this resource.");
    }
}