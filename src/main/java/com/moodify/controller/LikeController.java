package com.moodify.controller;

import com.moodify.dto.LikeCountResponse;
import com.moodify.service.LikeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts/{postId}/likes")
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LikeCountResponse> likePost(@PathVariable Long postId) {
        // EntityNotFoundException -> 404
        LikeCountResponse response = likeService.likePost(postId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LikeCountResponse> unlikePost(@PathVariable Long postId) {
        // EntityNotFoundException -> 404
        LikeCountResponse response = likeService.unlikePost(postId);
        return ResponseEntity.ok(response);
    }
}