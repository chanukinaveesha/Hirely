package com.recruitsystem.avatar.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AvatarResponse {

    private boolean hasAvatar;
    private String imageUrl;
    private LocalDateTime updatedAt;
}
