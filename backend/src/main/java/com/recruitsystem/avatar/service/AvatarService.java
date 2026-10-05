package com.recruitsystem.avatar.service;

import com.recruitsystem.avatar.dto.AvatarImageResponse;
import com.recruitsystem.avatar.dto.AvatarResponse;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public interface AvatarService {

    AvatarResponse uploadOrReplace(Long userId, MultipartFile file);

    void remove(Long userId);

    AvatarResponse getMyAvatar(Long userId);

    AvatarImageResponse getImage(Long userId);

    /**
     * Called in-process by other platform services (post, dashboard,
     * publicprofile) to embed a user's avatar URL in their own responses
     * without an extra HTTP round-trip. Empty when the user has no avatar.
     */
    Optional<String> findImageUrl(Long userId);
}
