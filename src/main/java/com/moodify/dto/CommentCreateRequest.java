package com.moodify.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CommentCreateRequest {

    @NotBlank(message = "Comment content cannot be blank")
    private String content;
}