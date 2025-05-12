package com.moodify.repository;

import com.moodify.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> { // <Like, Long - тип ID лайка>

    /**
     * Ищет лайк по ID пользователя и ID поста.
     * @param userId ID пользователя.
     * @param postId ID поста.
     * @return Optional<Like>.
     */
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    /**
     * Проверяет, существует ли лайк по ID пользователя и ID поста.
     * @param userId ID пользователя.
     * @param postId ID поста.
     * @return true, если лайк существует, false иначе.
     */
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    /**
     * Подсчитывает количество лайков для поста.
     * @param postId ID поста.
     * @return Количество лайков (long).
     */
    long countByPostId(Long postId);

    /**
     * Удаляет лайк по ID пользователя и ID поста (используется для анлайка).
     * @param userId ID пользователя.
     * @param postId ID поста.
     */
    @Transactional
    void deleteByUserIdAndPostId(Long userId, Long postId);

    /**
     * Удаляет все лайки, поставленные пользователем с указанным ID.
     * Этот метод будет использоваться в UserService при удалении пользователя.
     * @param userId ID пользователя, чьи лайки нужно удалить.
     */
    @Transactional // Указываем, что это изменяющая транзакционная операция
    void deleteByUserId(Long userId); // <-- ДОБАВЛЕННЫЙ/ПРОВЕРЯЕМЫЙ МЕТОД
}