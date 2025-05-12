package com.moodify.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private AuthorDto author;
    private long likeCount;
    private long commentCount;

    private String songUrl;
}