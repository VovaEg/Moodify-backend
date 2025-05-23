package com.moodify.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private AuthorDto author;
}