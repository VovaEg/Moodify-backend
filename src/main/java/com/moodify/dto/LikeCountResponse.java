package com.moodify.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class LikeCountResponse {
    private long likeCount; // New number of likes
}