package com.moodify.service;

import com.moodify.dto.RegisterRequest;
import com.moodify.model.ERole;
import com.moodify.model.Role;
import com.moodify.model.User;
import com.moodify.repository.RoleRepository;
import com.moodify.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${moodify.app.admin.username}")
    private String adminUsername;
    @Value("${moodify.app.admin.email}")
    private String adminEmail;
    @Value("${moodify.app.admin.password}")
    private String adminPassword;

    @Autowired
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        log.info("Registering user: {}", registerRequest.getUsername());
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(true);
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
        roles.add(userRole);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    @PostConstruct
    @Transactional
    protected void initializeAdminUser() {
        log.info("Initializing roles and admin user...");

        // Create/find roles
        Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseGet(() -> {
            log.info("Role USER not found in PostConstruct. Creating...");
            return roleRepository.save(new Role(null, ERole.ROLE_USER));
        });
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseGet(() -> {
            log.info("Role ADMIN not found in PostConstruct. Creating...");
            return roleRepository.save(new Role(null, ERole.ROLE_ADMIN));
        });
        // Roles created and found

        // Check and create an admin
        log.info("Checking for admin user '{}'...", adminUsername);
        if (!userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user '{}' not found. Creating admin user...", adminUsername);
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword)); // Hash the admin password
            adminUser.setEnabled(true);

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            log.info("Admin user '{}' created successfully with roles: {}", adminUsername, roles.stream().map(Role::getName).collect(Collectors.toList()));
        } else {
            log.info("Admin user '{}' already exists.", adminUsername);
        }
        log.info("Roles and admin user initialization complete.");
    }
}