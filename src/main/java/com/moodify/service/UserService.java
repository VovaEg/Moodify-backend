package com.moodify.service;

import com.moodify.dto.UserResponseDto;
import com.moodify.model.Post;
import com.moodify.model.User;
import com.moodify.repository.CommentRepository;
import com.moodify.repository.LikeRepository;
import com.moodify.repository.PostRepository;
import com.moodify.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       PostRepository postRepository,
                       LikeRepository likeRepository,
                       CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
    }

    // Mapping User -> UserResponseDto
    private UserResponseDto mapUserToUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        logger.debug("Admin fetching all users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(this::mapUserToUserResponseDto);
    }

    @Transactional
    public void deleteUser(Long userId) {
        logger.warn("[ADMIN ACTION] Attempting to delete user id: {}", userId);


        // Find the user
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Find and delete all posts of this user
        List<Post> userPosts = postRepository.findByUserId(userId);
        if (!userPosts.isEmpty()) {
            logger.warn("Deleting {} posts associated with user id: {}", userPosts.size(), userId);

            // Delete posts one by one so that cascade operations of JPA work
            for (Post post : userPosts) {
                logger.debug("Deleting post with id: {} (belonging to user id: {})", post.getId(), userId);
                postRepository.delete(post); // Delete each post individually
            }
        } else {
            logger.info("No posts found for user id: {} to delete.", userId);
        }

        // Delete likes left by this user (under any posts)
        logger.warn("Deleting likes made by user id: {}", userId);
        likeRepository.deleteByUserId(userId); // Вызываем новый метод репозитория

        // Delete comments left by this user (under any posts)
        logger.warn("Deleting comments made by user id: {}", userId);
        commentRepository.deleteByUserId(userId);

        // Delete the user
        userRepository.delete(userToDelete);
        logger.info("User id: {} and their associated content (posts, likes, comments) deleted successfully by admin.", userId);
    }
}