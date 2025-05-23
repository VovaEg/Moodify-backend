package com.moodify.service;

import com.moodify.dto.LikeCountResponse;
import com.moodify.model.Like;
import com.moodify.model.Post;
import com.moodify.model.User;
import com.moodify.repository.LikeRepository;
import com.moodify.repository.PostRepository;
import com.moodify.security.AuthenticationHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public LikeService(LikeRepository likeRepository,
                       PostRepository postRepository,
                       AuthenticationHelper authenticationHelper) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.authenticationHelper = authenticationHelper;
    }

    @Transactional
    public LikeCountResponse likePost(Long postId) {
        User currentUser = authenticationHelper.getCurrentUserEntity();
        Long currentUserId = currentUser.getId();
        log.info("User id:{} ('{}') attempting to like post id: {}", currentUserId, currentUser.getUsername(), postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot like post. Post not found with id: " + postId));

        boolean alreadyLiked = likeRepository.existsByUserIdAndPostId(currentUserId, postId);

        if (!alreadyLiked) {
            Like like = new Like();
            like.setUser(currentUser);
            like.setPost(post);
            likeRepository.save(like);
            log.info("Like successfully added by user id:{} ('{}') for post id: {}", currentUserId, currentUser.getUsername(), postId);
        } else {
            log.warn("User id:{} ('{}') already liked post id: {}. No action taken.", currentUserId, currentUser.getUsername(), postId);
        }

        long likeCount = likeRepository.countByPostId(postId);
        return new LikeCountResponse(likeCount);
    }

    @Transactional
    public LikeCountResponse unlikePost(Long postId) {
        User currentUser = authenticationHelper.getCurrentUserEntity();
        Long currentUserId = currentUser.getId();
        log.info("User id:{} ('{}') attempting to unlike post id: {}", currentUserId, currentUser.getUsername(), postId);

        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Cannot unlike post. Post not found with id: " + postId);
        }

        boolean existed = likeRepository.existsByUserIdAndPostId(currentUserId, postId);
        if (existed) {
            likeRepository.deleteByUserIdAndPostId(currentUserId, postId);
            log.info("Like successfully removed by user id:{} ('{}') for post id: {}", currentUserId, currentUser.getUsername(), postId);
        } else {
            log.warn("User id:{} ('{}') tried to unlike post id: {}, but no like was found.", currentUserId, currentUser.getUsername(), postId);
        }

        long likeCount = likeRepository.countByPostId(postId);
        return new LikeCountResponse(likeCount);
    }
}