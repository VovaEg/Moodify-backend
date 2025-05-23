package com.moodify.config;

import com.moodify.model.ERole;
import com.moodify.model.Role;
import com.moodify.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Autowired
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking initial roles...");
        createRoleIfNotFound(ERole.ROLE_USER);
        createRoleIfNotFound(ERole.ROLE_ADMIN); // Return creation ROLE_ADMIN
        log.info("Initial roles check complete.");
    }

    private Role createRoleIfNotFound(ERole roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            log.info("Creating role: {}", roleName);
            return roleRepository.save(new Role(null, roleName));
        });
    }
}