package com.moodify.controller;

import com.moodify.dto.MessageResponse;
import com.moodify.dto.PostCreateRequest;
import com.moodify.dto.PostResponse;
import com.moodify.dto.PostUpdateRequest;
import com.moodify.service.PostService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest postCreateRequest) {
        PostResponse createdPost = postService.createPost(postCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable)
    {
        Page<PostResponse> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        // EntityNotFoundException will be processed RestExceptionHandler -> 404
        PostResponse post = postService.getPostById(postId);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId,
                                                   @Valid @RequestBody PostUpdateRequest postUpdateRequest) {
        // EntityNotFoundException -> 404
        // AccessDeniedException -> 403
        // MethodArgumentNotValidException -> 400
        PostResponse updatedPost = postService.updatePost(postId, postUpdateRequest);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deletePost(@PathVariable Long postId) {
        // EntityNotFoundException -> 404
        // AccessDeniedException -> 403
        postService.deletePost(postId);
        return ResponseEntity.ok(new MessageResponse("Post deleted successfully!"));
    }
}