package com.recruitsystem.post.dto;

import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.post.entity.PostType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private PostType type;
    private String body;

    private Long authorId;
    private String authorName;
    private UserRole authorRole;
    private String authorAvatarUrl;

    // Null when the post has no image.
    private String imageUrl;

    // Where clicking the post should navigate: the vacancy detail page for
    // VACANCY_PROMO, the author's public profile for PROFILE_SHOWCASE.
    private String linkUrl;

    private LocalDateTime createdAt;
}
