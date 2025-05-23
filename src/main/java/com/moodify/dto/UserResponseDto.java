package com.moodify.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private LocalDateTime createdAt;
    private List<String> roles;
}