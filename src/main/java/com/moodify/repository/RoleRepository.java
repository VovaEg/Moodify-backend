package com.moodify.repository;

import com.moodify.model.ERole;
import com.moodify.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    // Метод для пошука ролі по її імені (Enum)
    Optional<Role> findByName(ERole name);
}