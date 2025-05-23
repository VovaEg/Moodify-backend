package com.moodify.config;

public final class EndpointConstants {

    private EndpointConstants() {
    }

    // API Base Path
    public static final String API_BASE = "/api";

    // AuthController Endpoints
    public static final String AUTH_CONTROLLER_BASE_PATH = API_BASE + "/auth";
    public static final String AUTH_REGISTER_ENDPOINT = "/register";         // POST
    public static final String AUTH_LOGIN_ENDPOINT = "/login";            // POST

    // PostController Endpoints
    public static final String POST_CONTROLLER_BASE_PATH = API_BASE + "/posts";
    public static final String POST_BY_ID_ENDPOINT = "/{postId}";           // GET, PUT, DELETE

    // CommentController Endpoints
    public static final String COMMENTS_FOR_POST_ENDPOINT = "/posts/{postId}/comments"; // POST, GET /api/posts/{postId}/comments
    public static final String COMMENT_BY_ID_OPERATIONS_ENDPOINT = "/comments/{commentId}"; // DELETE /api/comments/{commentId}

    // LikeController Endpoints
    public static final String LIKES_FOR_POST_BASE_PATH = API_BASE + "/posts/{postId}/likes";


    // AdminController Endpoints
    public static final String ADMIN_CONTROLLER_BASE_PATH = API_BASE + "/admin";
    public static final String ADMIN_DELETE_POST_ENDPOINT = "/posts/{postId}";         // DELETE
    public static final String ADMIN_DELETE_COMMENT_ENDPOINT = "/comments/{commentId}"; // DELETE
    public static final String ADMIN_GET_ALL_USERS_ENDPOINT = "/users";                // GET
    public static final String ADMIN_DELETE_USER_ENDPOINT = "/users/{userId}";         // DELETE

}