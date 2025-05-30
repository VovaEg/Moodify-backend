package com.moodify.security;

import com.moodify.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt;
        try {
            jwt = parseJwt(request);
            log.debug("AuthTokenFilter: Parsed JWT: {}", (jwt != null ? jwt.substring(0, Math.min(jwt.length(), 10)) + "..." : "null"));

            if (jwt != null) { // If the token is removed
                boolean isValid = jwtUtils.validateJwtToken(jwt); // Check the validity
                log.debug("AuthTokenFilter: JWT validation result: {}", isValid);

                if (isValid) { // If the token is valid
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    log.debug("AuthTokenFilter: Username from JWT: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    // Create an authentication object
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication to the context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("AuthTokenFilter: Set Authentication in SecurityContext for user '{}'", username);
                } else {
                    log.warn("AuthTokenFilter: Invalid JWT token received.");
                }
            } else {
                log.debug("AuthTokenFilter: No JWT found in Authorization header.");
            }
        } catch (Exception e) {
            log.error("AuthTokenFilter: Cannot set user authentication: {}", e.getMessage(), e);
        }

        // Pass the request/response further along the filter chain
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Return the part after "Bearer "
        }
        return null; // Return null if the header is not found or is invalid
    }
}