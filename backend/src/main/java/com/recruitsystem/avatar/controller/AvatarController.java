package com.recruitsystem.avatar.controller;

import com.recruitsystem.avatar.dto.AvatarImageResponse;
import com.recruitsystem.avatar.dto.AvatarResponse;
import com.recruitsystem.avatar.service.AvatarService;
import com.recruitsystem.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/avatars")
@RequiredArgsConstructor
@Tag(name = "Avatars")
public class AvatarController {

    private final AvatarService avatarService;

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarResponse> uploadOrReplace(
            @AuthenticationPrincipal UserPrincipal principal, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(avatarService.uploadOrReplace(principal.getId(), file));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal principal) {
        avatarService.remove(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AvatarResponse> getMyAvatar(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(avatarService.getMyAvatar(principal.getId()));
    }

    // Unauthenticated on purpose — <img src> can't send an Authorization
    // header. Only this binary GET route is public; upload/replace/remove
    // above stay authenticated and owner-only (see SecurityConfig).
    @GetMapping("/{userId}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long userId) {
        AvatarImageResponse image = avatarService.getImage(userId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getResource());
    }
}
