package com.moodify.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AuthorDto {
    private Long id;
    private String username;
}