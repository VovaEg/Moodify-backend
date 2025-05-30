package com.moodify.controller;

import com.moodify.dto.CommentCreateRequest;
import com.moodify.dto.CommentResponse;
import com.moodify.dto.MessageResponse;
import com.moodify.service.CommentService;
import static com.moodify.config.EndpointConstants.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(API_BASE)
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping(COMMENTS_FOR_POST_ENDPOINT)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long postId,
                                                         @Valid @RequestBody CommentCreateRequest commentDto) {
        // EntityNotFoundException -> 404 (if post not found)
        CommentResponse createdComment = commentService.createComment(postId, commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @GetMapping(COMMENTS_FOR_POST_ENDPOINT)
    public ResponseEntity<Page<CommentResponse>> getCommentsByPostId(
            @PathVariable Long postId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable)
    {
        // EntityNotFoundException -> 404
        Page<CommentResponse> comments = commentService.getCommentsByPost(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping(COMMENT_BY_ID_OPERATIONS_ENDPOINT)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable Long commentId) {
        // EntityNotFoundException -> 404
        // AccessDeniedException -> 403
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully!"));
    }
}