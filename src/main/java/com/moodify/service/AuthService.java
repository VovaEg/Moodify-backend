package com.moodify.service;

import com.moodify.dto.RegisterRequest;
import com.moodify.model.ERole;
import com.moodify.model.Role;
import com.moodify.model.User;
import com.moodify.repository.RoleRepository;
import com.moodify.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

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
        logger.info("Registering user: {}", registerRequest.getUsername());
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
        logger.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    @PostConstruct
    @Transactional
    protected void initializeAdminUser() {
        logger.info("Initializing roles and admin user...");

        // Створюєм/Знаходимо ролі
        Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseGet(() -> {
            logger.info("Role USER not found in PostConstruct. Creating...");
            return roleRepository.save(new Role(null, ERole.ROLE_USER));
        });
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseGet(() -> {
            logger.info("Role ADMIN not found in PostConstruct. Creating...");
            return roleRepository.save(new Role(null, ERole.ROLE_ADMIN));
        });
        // Ролі створені або знайдені

        // Перевіряємо і створюєм адміна
        logger.info("Checking for admin user '{}'...", adminUsername);
        if (!userRepository.existsByUsername(adminUsername)) {
            logger.info("Admin user '{}' not found. Creating admin user...", adminUsername);
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword)); // Хешуємо пароль адміна
            adminUser.setEnabled(true);

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            logger.info("Admin user '{}' created successfully with roles: {}", adminUsername, roles.stream().map(Role::getName).collect(Collectors.toList()));
        } else {
            logger.info("Admin user '{}' already exists.", adminUsername);
        }
        logger.info("Roles and admin user initialization complete.");
    }
}