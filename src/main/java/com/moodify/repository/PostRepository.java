package com.moodify.repository;

import com.moodify.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List; // <-- Импорт для List

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user ORDER BY p.createdAt DESC")
    Page<Post> findAllWithUserOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Находит все посты, созданные пользователем с указанным ID.
     * Используется в UserService для удаления постов пользователя перед удалением самого пользователя.
     * @param userId ID пользователя.
     * @return Список постов пользователя.
     */
    List<Post> findByUserId(Long userId); // <-- ДОБАВЛЕННЫЙ МЕТОД
}