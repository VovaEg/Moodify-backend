package com.moodify.repository;

import com.moodify.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> { // <Like, Long - тип ID лайку>

    // Пошук лайку по ID користувача та ID посту
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    // Перевірка існування лайку по ID користувача і ID посту
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // Підрахунок лайуів для конкретного посту
    long countByPostId(Long postId);

    // Метод для видалення лайка по ID користувача і ID посту
    @Transactional
    void deleteByUserIdAndPostId(Long userId, Long postId);
}