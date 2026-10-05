package com.recruitsystem.avatar.service;

import com.recruitsystem.avatar.dto.AvatarImageResponse;
import com.recruitsystem.avatar.dto.AvatarResponse;
import com.recruitsystem.avatar.entity.UserAvatar;
import com.recruitsystem.avatar.repository.UserAvatarRepository;
import com.recruitsystem.entity.auth.User;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.media.ImageContentType;
import com.recruitsystem.media.ImageValidationUtils;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.service.storage.FileStorageService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {

    private static final String STORAGE_SUBDIRECTORY = "avatars";

    private final UserAvatarRepository userAvatarRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public AvatarResponse uploadOrReplace(Long userId, MultipartFile file) {
        ImageContentType imageType = ImageValidationUtils.validate(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String storedFilename = fileStorageService.store(file, STORAGE_SUBDIRECTORY);

        UserAvatar avatar = userAvatarRepository.findByUserId(userId).orElse(null);
        if (avatar == null) {
            avatar = UserAvatar.builder().user(user).build();
        } else {
            fileStorageService.delete(STORAGE_SUBDIRECTORY, avatar.getStoredFilename());
        }

        avatar.setStoredFilename(storedFilename);
        avatar.setOriginalFilename(file.getOriginalFilename());
        avatar.setContentType(imageType.getMimeType());
        avatar.setFileSizeBytes(file.getSize());

        return toResponse(userAvatarRepository.save(avatar));
    }

    @Override
    @Transactional
    public void remove(Long userId) {
        UserAvatar avatar = userAvatarRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No avatar on file"));
        fileStorageService.delete(STORAGE_SUBDIRECTORY, avatar.getStoredFilename());
        userAvatarRepository.delete(avatar);
    }

    @Override
    @Transactional(readOnly = true)
    public AvatarResponse getMyAvatar(Long userId) {
        return userAvatarRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> AvatarResponse.builder().hasAvatar(false).build());
    }

    @Override
    @Transactional(readOnly = true)
    public AvatarImageResponse getImage(Long userId) {
        UserAvatar avatar = userAvatarRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No avatar on file for this user"));
        Resource resource = fileStorageService.load(STORAGE_SUBDIRECTORY, avatar.getStoredFilename());
        return AvatarImageResponse.builder()
                .resource(resource)
                .contentType(avatar.getContentType())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findImageUrl(Long userId) {
        if (!userAvatarRepository.findByUserId(userId).isPresent()) {
            return Optional.empty();
        }
        return Optional.of("/avatars/" + userId + "/image");
    }

    private AvatarResponse toResponse(UserAvatar avatar) {
        return AvatarResponse.builder()
                .hasAvatar(true)
                .imageUrl("/avatars/" + avatar.getUser().getId() + "/image")
                .updatedAt(avatar.getUpdatedAt())
                .build();
    }
}
