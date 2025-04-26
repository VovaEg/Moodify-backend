package com.moodify.repository;

import com.moodify.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> { // <Comment, Long - тип ID комментаря>

    // Пошук комментарів для конкретного посту с пагинацією і сортуванням за зростанням дати
    Page<Comment> findByPostIdOrderByCreatedAtAsc(Long postId, Pageable pageable);

    // Метод для підрахунку комментарів до посту
    long countByPostId(Long postId);
}