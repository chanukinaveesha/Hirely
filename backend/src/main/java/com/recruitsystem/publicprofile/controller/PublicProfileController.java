package com.recruitsystem.publicprofile.controller;

import com.recruitsystem.publicprofile.dto.PublicProfileResponse;
import com.recruitsystem.publicprofile.service.PublicProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Requires a JWT like the rest of the app (every logged-in role can view
// it) — "public" here means "safe fields only", not "unauthenticated".
@RestController
@RequestMapping("/api/public-profiles")
@RequiredArgsConstructor
@Tag(name = "Public Profiles")
public class PublicProfileController {

    private final PublicProfileService publicProfileService;

    @GetMapping("/{userId}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(publicProfileService.getPublicProfile(userId));
    }
}
