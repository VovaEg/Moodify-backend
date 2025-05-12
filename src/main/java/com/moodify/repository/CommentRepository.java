package com.moodify.repository;

import com.moodify.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // <-- Импорт для @Transactional

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> { // <Comment, Long - тип ID комментария>

    /**
     * Ищет комментарии для конкретного поста с пагинацией и сортировкой
     * по дате создания по возрастанию.
     * @param postId ID поста.
     * @param pageable Параметры пагинации и сортировки.
     * @return Страница (Page) с комментариями.
     */
    Page<Comment> findByPostIdOrderByCreatedAtAsc(Long postId, Pageable pageable);

    /**
     * Подсчитывает количество комментариев для конкретного поста.
     * @param postId ID поста.
     * @return Количество комментариев (long).
     */
    long countByPostId(Long postId);

    /**
     * Удаляет все комментарии, оставленные пользователем с указанным ID.
     * Этот метод будет использоваться в UserService при удалении пользователя,
     * чтобы избежать ошибок нарушения внешнего ключа.
     * @param userId ID пользователя, чьи комментарии нужно удалить.
     */
    @Transactional // Указываем, что это изменяющая транзакционная операция
    void deleteByUserId(Long userId); // <-- ДОБАВЛЕННЫЙ/ПРОВЕРЯЕМЫЙ МЕТОД
}