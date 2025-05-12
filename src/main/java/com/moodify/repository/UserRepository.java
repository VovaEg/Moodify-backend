package com.moodify.repository;

import com.moodify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Search for a user by name (for login and UserDetailsService)
    Optional<User> findByUsername(String username);

    // Checking the existence of a user by name (for registration)
    Boolean existsByUsername(String username);

    // Checking the user's existence by email (for registration)
    Boolean existsByEmail(String email);
}