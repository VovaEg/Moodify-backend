package com.moodify.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PostResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private AuthorDto author;
    private long likeCount;
    private long commentCount;

    private String songUrl;
}