package com.moodify.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {

        // Log an unauthorized access attempt
        log.warn("Access Denied for user trying to access {}: {}", request.getRequestURI(), accessDeniedException.getMessage());

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: You do not have the required permissions to access this resource.");
    }
}