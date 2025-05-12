package com.moodify.controller;

import com.moodify.dto.MessageResponse;
import com.moodify.dto.UserResponseDto;
import com.moodify.service.CommentService;
import com.moodify.service.PostService;
import com.moodify.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public AdminController(PostService postService,
                           CommentService commentService,
                           UserService userService) {
        this.postService = postService;
        this.commentService = commentService;
        this.userService = userService;
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deletePostAsAdmin(@PathVariable Long postId) {
        logger.warn("[ADMIN ACTION] Admin attempting to delete post id: {}", postId);
        postService.deletePost(postId);
        return ResponseEntity.ok(new MessageResponse("Post with id " + postId + " deleted successfully by admin."));
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCommentAsAdmin(@PathVariable Long commentId) {
        logger.warn("[ADMIN ACTION] Admin attempting to delete comment id: {}", commentId);
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new MessageResponse("Comment with id " + commentId + " deleted successfully by admin."));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        logger.info("[ADMIN ACTION] Admin fetching user list");
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUserAsAdmin(@PathVariable Long userId) {
        logger.warn("[ADMIN ACTION] Admin attempting to delete user id: {}", userId);
        // Call the service method. Errors (NotFound, etc.) will be caught by RestExceptionHandler
        userService.deleteUser(userId);
        // Return a successfull response
        return ResponseEntity.ok(new MessageResponse("User with id " + userId + " and their associated posts deleted successfully by admin."));
    }
}