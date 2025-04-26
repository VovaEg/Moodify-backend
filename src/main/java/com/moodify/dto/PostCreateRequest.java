package com.moodify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCreateRequest {

    @NotBlank(message = "Content cannot be empty")
    private String content; // Текст настрою

    // Поки тільки URL пісні
    @Size(max = 2048, message = "Song URL cannot exceed 2048 characters")
    private String songUrl;
}