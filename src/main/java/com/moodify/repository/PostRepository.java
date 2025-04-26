package com.moodify.repository;

import com.moodify.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> { // <Post, Long - тип ID посту>

    // Кастомний запит для отримання сторінки постів с передзавантаженням (fetch)
    // информації про користувача (автора).Сортування за спаданням дати створення.
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user ORDER BY p.createdAt DESC")
    Page<Post> findAllWithUserOrderByCreatedAtDesc(Pageable pageable);
}