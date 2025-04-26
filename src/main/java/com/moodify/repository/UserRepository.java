package com.moodify.repository;

import com.moodify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> { // <User, Long - тип ID користувача>

    // Пошук користувача по імені (для логіну і UserDetailsService)
    Optional<User> findByUsername(String username);

    // Перевірка існування користувача по імені (для реєстрації)
    Boolean existsByUsername(String username);

    // Перевірка існування користувача по email (для реєстрації)
    Boolean existsByEmail(String email);
}