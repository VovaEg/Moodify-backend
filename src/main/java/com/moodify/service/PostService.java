package com.moodify.service;

import com.moodify.dto.AuthorDto;
import com.moodify.dto.PostCreateRequest;
import com.moodify.dto.PostResponse;
import com.moodify.dto.PostUpdateRequest;
import com.moodify.model.ERole;
import com.moodify.model.Post;
import com.moodify.model.User;
import com.moodify.repository.CommentRepository;
import com.moodify.repository.LikeRepository;
import com.moodify.repository.PostRepository;
import com.moodify.security.AuthenticationHelper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public PostService(PostRepository postRepository,
                       LikeRepository likeRepository,
                       CommentRepository commentRepository,
                       AuthenticationHelper authenticationHelper) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.authenticationHelper = authenticationHelper;
    }

    private PostResponse mapPostToPostResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setSongUrl(post.getSongUrl());
        response.setCreatedAt(post.getCreatedAt());
        User author = post.getUser();
        if (author != null) {
            response.setAuthor(new AuthorDto(author.getId(), author.getUsername()));
        } else {
            response.setAuthor(new AuthorDto(null, "Unknown"));
        }
        response.setLikeCount(likeRepository.countByPostId(post.getId()));
        response.setCommentCount(commentRepository.countByPostId(post.getId()));
        return response;
    }

    @Transactional
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        User currentUser = authenticationHelper.getCurrentUserEntity();
        logger.info("User '{}' is creating a post", currentUser.getUsername());
        Post post = new Post();
        post.setUser(currentUser);
        post.setContent(postCreateRequest.getContent());
        post.setSongUrl(postCreateRequest.getSongUrl());
        Post savedPost = postRepository.save(post);
        logger.info("Post created with id: {}", savedPost.getId());
        return mapPostToPostResponse(savedPost);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        logger.debug("Fetching all posts, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Post> postPage = postRepository.findAllWithUserOrderByCreatedAtDesc(pageable);
        return postPage.map(this::mapPostToPostResponse);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId) {
        logger.debug("Fetching post by id: {}", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        return mapPostToPostResponse(post);
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest updateRequest) {
        User currentUser = authenticationHelper.getCurrentUserEntity();
        logger.info("User '{}' attempting to update post id: {}", currentUser.getUsername(), postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        if (!post.getUser().getId().equals(currentUser.getId())) {
            logger.error("Access Denied: User '{}' is not the author of post id: {}", currentUser.getUsername(), postId);
            throw new AccessDeniedException("You are not authorized to update this post");
        }
        post.setContent(updateRequest.getContent());
        post.setSongUrl(updateRequest.getSongUrl());
        Post updatedPost = postRepository.save(post);
        logger.info("Post id: {} updated successfully by user '{}'", postId, currentUser.getUsername());
        return mapPostToPostResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long postId) {
        User currentUser = authenticationHelper.getCurrentUserEntity();
        logger.warn("User '{}' attempting to delete post id: {}", currentUser.getUsername(), postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
        boolean isAuthor = post.getUser().getId().equals(currentUser.getId());

        if (!isAuthor && !isAdmin) {
            logger.error("Access Denied: User '{}' is not authorized to delete post id {} (author id: {})",
                    currentUser.getUsername(), postId, post.getUser().getId());
            throw new AccessDeniedException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
        logger.info("Post id: {} deleted successfully by user '{}' (is admin: {})", postId, currentUser.getUsername(), isAdmin);
    }
}