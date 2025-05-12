package com.moodify.repository;

import com.moodify.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> { // <Like, Long - тип ID лайка>

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    @Transactional
    void deleteByUserIdAndPostId(Long userId, Long postId);

    @Transactional
    void deleteByUserId(Long userId);
}