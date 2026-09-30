package com.recruitsystem.controller.interview;

import com.recruitsystem.dto.interview.CandidateRescheduleRequest;
import com.recruitsystem.dto.interview.InterviewFeedbackResponse;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.dto.interview.PanelMemberSummaryResponse;
import com.recruitsystem.dto.interview.ProposeInterviewRequest;
import com.recruitsystem.dto.interview.RescheduleInterviewRequest;
import com.recruitsystem.dto.interview.SubmitFeedbackRequest;
import com.recruitsystem.security.UserPrincipal;
import com.recruitsystem.service.interview.InterviewService;
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
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Tag(name = "Interviews")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<InterviewResponse> propose(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ProposeInterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewService.propose(principal.getId(), request));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<InterviewResponse> reschedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RescheduleInterviewRequest request) {
        return ResponseEntity.ok(interviewService.reschedule(principal.getId(), id, request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<InterviewResponse> cancel(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(interviewService.cancel(principal.getId(), id));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<InterviewResponse> accept(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(interviewService.accept(principal.getId(), id));
    }

    @PutMapping("/{id}/request-reschedule")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<InterviewResponse> requestReschedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CandidateRescheduleRequest request) {
        return ResponseEntity.ok(interviewService.requestReschedule(principal.getId(), id, request));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<InterviewResponse>> getMyInterviews(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(interviewService.getMyInterviews(principal.getId()));
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<List<InterviewResponse>> getInterviewsForApplication(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long applicationId) {
        return ResponseEntity.ok(interviewService.getInterviewsForApplication(principal.getId(), applicationId));
    }

    @GetMapping("/panel")
    @PreAuthorize("hasRole('INTERVIEW_PANEL_MEMBER')")
    public ResponseEntity<List<InterviewResponse>> getMyPanelInterviews(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(interviewService.getMyPanelInterviews(principal.getId()));
    }

    @GetMapping("/panel-members")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<List<PanelMemberSummaryResponse>> listPanelMembers() {
        return ResponseEntity.ok(interviewService.listPanelMembers());
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('INTERVIEW_PANEL_MEMBER')")
    public ResponseEntity<InterviewFeedbackResponse> submitFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        return ResponseEntity.ok(interviewService.submitFeedback(principal.getId(), id, request));
    }

    @GetMapping("/{id}/feedback")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<List<InterviewFeedbackResponse>> getFeedbackForInterview(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getFeedbackForInterview(principal.getId(), id));
    }
}
