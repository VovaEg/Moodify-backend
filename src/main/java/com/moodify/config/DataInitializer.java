package com.moodify.config;

import com.moodify.model.ERole;
import com.moodify.model.Role;
import com.moodify.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final RoleRepository roleRepository;

    @Autowired
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Checking initial roles...");
        createRoleIfNotFound(ERole.ROLE_USER);
        createRoleIfNotFound(ERole.ROLE_ADMIN); // Return creation ROLE_ADMIN
        logger.info("Initial roles check complete.");
    }

    private Role createRoleIfNotFound(ERole roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            logger.info("Creating role: {}", roleName);
            return roleRepository.save(new Role(null, roleName));
        });
    }
}