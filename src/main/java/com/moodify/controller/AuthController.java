package com.moodify.controller;

import com.moodify.dto.JwtResponse;
import com.moodify.dto.LoginRequest;
import com.moodify.dto.RegisterRequest;
import com.moodify.dto.MessageResponse;
import com.moodify.model.User;
import static com.moodify.config.EndpointConstants.*;
import com.moodify.repository.UserRepository;
import com.moodify.security.JwtUtils;
import com.moodify.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600) // CORS settings
@RestController
@RequestMapping(AUTH_CONTROLLER_BASE_PATH) // Base path for authentication
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository; // Use to get id/email

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

    @PostMapping(AUTH_REGISTER_ENDPOINT)
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping(AUTH_LOGIN_ENDPOINT)
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Attempting to authenticate user: {}", loginRequest.getUsername());

        // AuthenticationManager checks the login/password
        // Throws AuthenticationException on error -> AuthEntryPointJwt will return 401
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        log.info("Authentication successful for user: {}", loginRequest.getUsername());

        // Set authentication to the context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Generate token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Collect information for the answer
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found after authentication."));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Return the response with the token and user data
        JwtResponse jwtResponse = new JwtResponse(jwt, user.getId(), userDetails.getUsername(), user.getEmail(), roles);
        return ResponseEntity.ok(jwtResponse);
    }
}