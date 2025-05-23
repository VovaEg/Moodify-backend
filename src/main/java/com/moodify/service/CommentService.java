package com.moodify.service;

import com.moodify.dto.AuthorDto;
import com.moodify.dto.CommentCreateRequest;
import com.moodify.dto.CommentResponse;
import com.moodify.model.Comment;
import com.moodify.model.ERole;
import com.moodify.model.Post;
import com.moodify.model.User;
import com.moodify.repository.CommentRepository;
import com.moodify.repository.PostRepository;
import com.moodify.security.AuthenticationHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          AuthenticationHelper authenticationHelper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.authenticationHelper = authenticationHelper;
    }

    private CommentResponse mapCommentToCommentResponse(Comment comment) {
        CommentResponse dto = new CommentResponse();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        User author = comment.getUser();
        if (author != null) {
            dto.setAuthor(new AuthorDto(author.getId(), author.getUsername()));
        } else {
            dto.setAuthor(new AuthorDto(null, "Unknown"));
        }
        return dto;
    }

    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest commentDto) {
        User currentUser = authenticationHelper.getCurrentUserEntity();
        log.info("User '{}' creating comment for post {}", currentUser.getUsername(), postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot comment. Post not found with id: " + postId));
        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setUser(currentUser);
        comment.setPost(post);
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created with id: {} for post id: {}", savedComment.getId(), postId);
        return mapCommentToCommentResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable) {
        log.debug("Fetching comments for post id: {}", postId);
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Cannot get comments. Post not found with id: " + postId);
        }
        Page<Comment> commentsPage = commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable);
        log.debug("Found {} comments on page {} for post id: {}", commentsPage.getNumberOfElements(), pageable.getPageNumber(), postId);
        return commentsPage.map(this::mapCommentToCommentResponse);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = authenticationHelper.getCurrentUserEntity();
        log.warn("User '{}' attempting to delete comment id: {}", currentUser.getUsername(), commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
        boolean isAuthor = (comment.getUser() != null) && (comment.getUser().getId().equals(currentUser.getId()));

        if (!isAuthor && !isAdmin) {
            log.error("Access Denied: User '{}' is not authorized to delete comment id {} (author id: {})",
                    currentUser.getUsername(), commentId, comment.getUser() != null ? comment.getUser().getId() : "null");
            throw new AccessDeniedException("You are not authorized to delete this comment");
        }
        log.info("Authorization successful. User '{}' deleting comment id: {}", currentUser.getUsername(), commentId);
        commentRepository.delete(comment);
    }
}