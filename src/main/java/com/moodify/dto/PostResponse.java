package com.moodify.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private AuthorDto author; // Інформація про автора
    private long likeCount;   // ККількість лайків
    private long commentCount;// Кількість коментарів

    // Інформація про пісню (поки тільки URL)
    private String songUrl;
}