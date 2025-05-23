package com.moodify.controller;

import com.moodify.dto.MessageResponse;
import com.moodify.dto.UserResponseDto;
import com.moodify.service.CommentService;
import com.moodify.service.PostService;
import com.moodify.service.UserService;
import static com.moodify.config.EndpointConstants.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(ADMIN_CONTROLLER_BASE_PATH)
public class AdminController {

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

    @DeleteMapping(ADMIN_DELETE_POST_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deletePostAsAdmin(@PathVariable Long postId) {
        log.warn("[ADMIN ACTION] Admin attempting to delete post id: {}", postId);
        postService.deletePost(postId);
        return ResponseEntity.ok(new MessageResponse("Post with id " + postId + " deleted successfully by admin."));
    }

    @DeleteMapping(ADMIN_DELETE_COMMENT_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCommentAsAdmin(@PathVariable Long commentId) {
        log.warn("[ADMIN ACTION] Admin attempting to delete comment id: {}", commentId);
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new MessageResponse("Comment with id " + commentId + " deleted successfully by admin."));
    }

    @GetMapping(ADMIN_GET_ALL_USERS_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        log.info("[ADMIN ACTION] Admin fetching user list");
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping(ADMIN_DELETE_USER_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUserAsAdmin(@PathVariable Long userId) {
        log.warn("[ADMIN ACTION] Admin attempting to delete user id: {}", userId);
        // Call the service method. Errors (NotFound, etc.) will be caught by RestExceptionHandler
        userService.deleteUser(userId);
        // Return a successfull response
        return ResponseEntity.ok(new MessageResponse("User with id " + userId + " and their associated posts deleted successfully by admin."));
    }
}