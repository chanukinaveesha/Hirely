package com.recruitsystem.controller.resume;

import com.recruitsystem.dto.resume.ResumeDownloadResponse;
import com.recruitsystem.dto.resume.ResumeResponse;
import com.recruitsystem.security.UserPrincipal;
import com.recruitsystem.service.resume.ResumeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ResumeResponse> uploadOrReplace(
            @AuthenticationPrincipal UserPrincipal principal, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(resumeService.uploadOrReplace(principal.getId(), file));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ResumeResponse> getMyResume(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(resumeService.getMyResume(principal.getId()));
    }

    @GetMapping("/me/download")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Resource> downloadMyResume(@AuthenticationPrincipal UserPrincipal principal) {
        return buildDownloadResponse(resumeService.downloadMyResume(principal.getId()));
    }

    @GetMapping("/applications/{applicationId}/download")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<Resource> downloadForApplication(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long applicationId) {
        return buildDownloadResponse(resumeService.downloadForApplication(principal.getId(), applicationId));
    }

    private ResponseEntity<Resource> buildDownloadResponse(ResumeDownloadResponse download) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFilename() + "\"")
                .body(download.getResource());
    }
}
