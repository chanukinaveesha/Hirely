package com.recruitsystem.controller.assessment;

import com.recruitsystem.dto.assessment.AssessmentResponse;
import com.recruitsystem.dto.assessment.AssignAssessmentRequest;
import com.recruitsystem.dto.assessment.CandidateAssessmentResponse;
import com.recruitsystem.dto.assessment.CreateAssessmentRequest;
import com.recruitsystem.dto.assessment.EvaluateCandidateAssessmentRequest;
import com.recruitsystem.dto.assessment.SubmitCandidateAssessmentRequest;
import com.recruitsystem.security.UserPrincipal;
import com.recruitsystem.service.assessment.AssessmentService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping("/assessments")
    @PreAuthorize("hasRole('HR_EXECUTIVE')")
    public ResponseEntity<AssessmentResponse> createAssessment(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.createAssessment(principal.getId(), request));
    }

    @GetMapping("/assessments/mine")
    @PreAuthorize("hasRole('HR_EXECUTIVE')")
    public ResponseEntity<List<AssessmentResponse>> getMyAssessments(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(assessmentService.getMyAssessments(principal.getId()));
    }

    @PutMapping("/assessments/{id}/archive")
    @PreAuthorize("hasRole('HR_EXECUTIVE')")
    public ResponseEntity<AssessmentResponse> archiveAssessment(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.archiveAssessment(principal.getId(), id));
    }

    @PostMapping("/assessments/{id}/assign")
    @PreAuthorize("hasRole('HR_EXECUTIVE')")
    public ResponseEntity<List<CandidateAssessmentResponse>> assignAssessment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AssignAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.assignAssessment(principal.getId(), id, request));
    }

    @GetMapping("/candidate-assessments/mine")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<CandidateAssessmentResponse>> getMyCandidateAssessments(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(assessmentService.getMyCandidateAssessments(principal.getId()));
    }

    @PutMapping("/candidate-assessments/{id}/submit")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<CandidateAssessmentResponse> submitCandidateAssessment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SubmitCandidateAssessmentRequest request) {
        return ResponseEntity.ok(assessmentService.submitCandidateAssessment(principal.getId(), id, request));
    }

    @PutMapping("/candidate-assessments/{id}/evaluate")
    @PreAuthorize("hasRole('HR_EXECUTIVE')")
    public ResponseEntity<CandidateAssessmentResponse> evaluateCandidateAssessment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody EvaluateCandidateAssessmentRequest request) {
        return ResponseEntity.ok(assessmentService.evaluateCandidateAssessment(principal.getId(), id, request));
    }

    @GetMapping("/candidate-assessments/application/{applicationId}")
    @PreAuthorize("hasRole('HR_EXECUTIVE')")
    public ResponseEntity<List<CandidateAssessmentResponse>> getCandidateAssessmentsForApplication(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long applicationId) {
        return ResponseEntity.ok(assessmentService.getCandidateAssessmentsForApplication(principal.getId(), applicationId));
    }

    @GetMapping("/candidate-assessments/vacancy/{vacancyId}/compare")
    @PreAuthorize("hasRole('HR_EXECUTIVE')")
    public ResponseEntity<List<CandidateAssessmentResponse>> compareForVacancy(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long vacancyId) {
        return ResponseEntity.ok(assessmentService.compareForVacancy(principal.getId(), vacancyId));
    }
}
