package com.moodify.security;

import com.moodify.model.User;
import com.moodify.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationHelper {

    private final UserRepository userRepository;

    @Autowired
    public AuthenticationHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            log.warn("Attempted to get current user entity, but user is not authenticated.");
            throw new IllegalStateException("User is not authenticated, cannot get current user entity.");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Authenticated user '{}' not found in database.", username);
                    return new UsernameNotFoundException("Current user '" + username + "' not found in database");
                });
    }
}