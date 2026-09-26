package com.recruitsystem.controller.application;

import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.application.ApplyToVacancyRequest;
import com.recruitsystem.security.UserPrincipal;
import com.recruitsystem.service.application.ApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApplicationResponse> apply(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ApplyToVacancyRequest request) {
        ApplicationResponse response = applicationService.apply(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(applicationService.getMyApplications(principal.getId()));
    }

    @PutMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        applicationService.withdraw(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vacancy/{vacancyId}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<List<ApplicationResponse>> getApplicantsForVacancy(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long vacancyId) {
        return ResponseEntity.ok(applicationService.getApplicantsForVacancy(principal.getId(), vacancyId));
    }

    @PutMapping("/{id}/shortlist")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<ApplicationResponse> shortlist(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.shortlist(principal.getId(), id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<ApplicationResponse> reject(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.reject(principal.getId(), id));
    }

    @PutMapping("/{id}/select")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<ApplicationResponse> select(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.select(principal.getId(), id));
    }
}
