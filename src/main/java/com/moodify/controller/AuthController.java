package com.moodify.controller;

import com.moodify.dto.JwtResponse;
import com.moodify.dto.LoginRequest;
import com.moodify.dto.RegisterRequest;
import com.moodify.dto.MessageResponse;
import com.moodify.model.User;
import com.moodify.repository.UserRepository;
import com.moodify.security.JwtUtils;
import com.moodify.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@CrossOrigin(origins = "*", maxAge = 3600) // Налаштування CORS
@RestController
@RequestMapping("/api/auth") // Базовий шлях для аутентифікації
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository; // Використовуєм для отримання id/email

    @Autowired
    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user: {}", loginRequest.getUsername());

        // AuthenticationManager перевіряє логін/пароль.
        // При помилці викине AuthenticationException -> AuthEntryPointJwt поверне 401.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        logger.info("Authentication successful for user: {}", loginRequest.getUsername());

        // Встановлюєм аутентифікацію в контекст
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Генеруємо токен
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Збираєио інформацію для відповіді
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found after authentication."));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Повертвємо відповідь з токеном и даними користувача
        JwtResponse jwtResponse = new JwtResponse(jwt, user.getId(), userDetails.getUsername(), user.getEmail(), roles);
        return ResponseEntity.ok(jwtResponse);
    }
}