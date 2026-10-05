package com.recruitsystem.avatar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recruitsystem.avatar.dto.AvatarResponse;
import com.recruitsystem.avatar.entity.UserAvatar;
import com.recruitsystem.avatar.repository.UserAvatarRepository;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.service.storage.FileStorageService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class AvatarServiceImplTest {

    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00, 0x00};

    @Mock
    private UserAvatarRepository userAvatarRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private Resource loadedResource;

    private AvatarServiceImpl avatarService;

    @BeforeEach
    void setUp() {
        avatarService = new AvatarServiceImpl(userAvatarRepository, userRepository, fileStorageService);
    }

    private JobSeeker user() {
        return JobSeeker.builder()
                .id(1L)
                .name("Sam Seeker")
                .email("sam@example.com")
                .passwordHash("hashed")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    private UserAvatar existingAvatar() {
        return UserAvatar.builder()
                .id(50L)
                .user(user())
                .storedFilename("old-stored.jpg")
                .originalFilename("old-photo.jpg")
                .contentType("image/jpeg")
                .fileSizeBytes(1000)
                .build();
    }

    @Test
    void uploadOrReplace_createsNewAvatarWhenNoneExists() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(userAvatarRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(fileStorageService.store(eq(file), anyString())).thenReturn("new-stored.jpg");
        when(userAvatarRepository.save(any(UserAvatar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvatarResponse response = avatarService.uploadOrReplace(1L, file);

        assertThat(response.isHasAvatar()).isTrue();
        assertThat(response.getImageUrl()).isEqualTo("/avatars/1/image");
        verify(fileStorageService, never()).delete(anyString(), anyString());
    }

    @Test
    void uploadOrReplace_deletesOldFileWhenReplacing() {
        MockMultipartFile file = new MockMultipartFile("file", "new-photo.jpg", "image/jpeg", JPEG_BYTES);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(userAvatarRepository.findByUserId(1L)).thenReturn(Optional.of(existingAvatar()));
        when(fileStorageService.store(eq(file), anyString())).thenReturn("new-stored.jpg");
        when(userAvatarRepository.save(any(UserAvatar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        avatarService.uploadOrReplace(1L, file);

        verify(fileStorageService).delete("avatars", "old-stored.jpg");
    }

    @Test
    void uploadOrReplace_throwsForUnsupportedImage() {
        MockMultipartFile file = new MockMultipartFile("file", "script.exe", "application/octet-stream", JPEG_BYTES);

        assertThatThrownBy(() -> avatarService.uploadOrReplace(1L, file)).isInstanceOf(ValidationException.class);
    }

    @Test
    void remove_throwsWhenNoneOnFile() {
        when(userAvatarRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avatarService.remove(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void remove_deletesFileAndRow() {
        when(userAvatarRepository.findByUserId(1L)).thenReturn(Optional.of(existingAvatar()));

        avatarService.remove(1L);

        verify(fileStorageService).delete("avatars", "old-stored.jpg");
        verify(userAvatarRepository).delete(any(UserAvatar.class));
    }

    @Test
    void findImageUrl_returnsEmptyWhenNoAvatar() {
        when(userAvatarRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThat(avatarService.findImageUrl(1L)).isEmpty();
    }

    @Test
    void findImageUrl_returnsPathWhenAvatarExists() {
        when(userAvatarRepository.findByUserId(1L)).thenReturn(Optional.of(existingAvatar()));

        assertThat(avatarService.findImageUrl(1L)).contains("/avatars/1/image");
    }

    @Test
    void getImage_throwsWhenNoAvatar() {
        when(userAvatarRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avatarService.getImage(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getImage_returnsResourceAndContentType() {
        when(userAvatarRepository.findByUserId(1L)).thenReturn(Optional.of(existingAvatar()));
        when(fileStorageService.load("avatars", "old-stored.jpg")).thenReturn(loadedResource);

        var image = avatarService.getImage(1L);

        assertThat(image.getContentType()).isEqualTo("image/jpeg");
        assertThat(image.getResource()).isSameAs(loadedResource);
    }
}
