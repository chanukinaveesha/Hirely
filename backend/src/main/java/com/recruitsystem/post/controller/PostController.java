package com.recruitsystem.post.controller;

import com.recruitsystem.post.dto.PostFeedResponse;
import com.recruitsystem.post.dto.PostImageResponse;
import com.recruitsystem.post.dto.PostRequest;
import com.recruitsystem.post.dto.PostResponse;
import com.recruitsystem.post.service.PostService;
import com.recruitsystem.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts")
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @ModelAttribute PostRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(principal.getId(), request, image));
    }

    @GetMapping
    public ResponseEntity<PostFeedResponse> getFeed(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getFeed(page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        postService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    // Unauthenticated on purpose — see SecurityConfig's PUBLIC_PATHS.
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        PostImageResponse image = postService.getImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getResource());
    }
}
