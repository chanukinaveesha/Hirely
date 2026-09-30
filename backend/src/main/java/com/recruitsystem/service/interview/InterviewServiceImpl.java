package com.recruitsystem.service.interview;

import com.recruitsystem.dto.interview.CandidateRescheduleRequest;
import com.recruitsystem.dto.interview.InterviewFeedbackResponse;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.dto.interview.PanelMemberSummaryResponse;
import com.recruitsystem.dto.interview.ProposeInterviewRequest;
import com.recruitsystem.dto.interview.RescheduleInterviewRequest;
import com.recruitsystem.dto.interview.SubmitFeedbackRequest;
import com.recruitsystem.entity.application.Application;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.auth.InterviewPanelMember;
import com.recruitsystem.entity.interview.Interview;
import com.recruitsystem.entity.interview.InterviewFeedback;
import com.recruitsystem.entity.interview.InterviewStatus;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.application.ApplicationRepository;
import com.recruitsystem.repository.auth.InterviewPanelMemberRepository;
import com.recruitsystem.repository.interview.InterviewFeedbackRepository;
import com.recruitsystem.repository.interview.InterviewRepository;
import com.recruitsystem.service.application.ApplicationService;
import com.recruitsystem.service.notification.NotificationService;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private static final Set<ApplicationStatus> SCHEDULABLE_FROM =
            EnumSet.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEWING);

    private static final Set<InterviewStatus> ACCEPTABLE_FROM =
            EnumSet.of(InterviewStatus.PROPOSED, InterviewStatus.RESCHEDULE_REQUESTED);

    private final InterviewRepository interviewRepository;
    private final InterviewFeedbackRepository interviewFeedbackRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewPanelMemberRepository interviewPanelMemberRepository;
    private final ApplicationService applicationService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public InterviewResponse propose(Long recruiterId, ProposeInterviewRequest request) {
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + request.getApplicationId()));
        if (!application.getVacancy().getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You do not own this vacancy");
        }
        if (!SCHEDULABLE_FROM.contains(application.getStatus())) {
            throw new ValidationException("An interview can only be scheduled for a shortlisted or in-progress application");
        }

        Set<InterviewPanelMember> panelMembers = new HashSet<>(interviewPanelMemberRepository.findAllById(request.getPanelMemberIds()));
        if (panelMembers.size() != request.getPanelMemberIds().size()) {
            throw new ResourceNotFoundException("One or more panel members not found");
        }

        Interview interview = Interview.builder()
                .application(application)
                .scheduledAt(request.getScheduledAt())
                .location(request.getLocation())
                .status(InterviewStatus.PROPOSED)
                .panelMembers(panelMembers)
                .build();
        Interview saved = interviewRepository.save(interview);

        applicationService.markInterviewing(application.getId());

        String vacancyTitle = application.getVacancy().getTitle();
        notifyCandidate(application, "An interview has been scheduled for '" + vacancyTitle + "' on "
                + request.getScheduledAt() + ". Please confirm.");
        for (InterviewPanelMember panelMember : panelMembers) {
            notificationService.notify(panelMember.getId(), "INTERVIEW_ASSIGNED",
                    "You have been assigned to interview a candidate for '" + vacancyTitle + "' on " + request.getScheduledAt() + ".");
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public InterviewResponse reschedule(Long recruiterId, Long interviewId, RescheduleInterviewRequest request) {
        Interview interview = findOwnedInterview(recruiterId, interviewId);
        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new ValidationException("A cancelled interview cannot be rescheduled");
        }

        interview.setScheduledAt(request.getScheduledAt());
        if (request.getLocation() != null) {
            interview.setLocation(request.getLocation());
        }
        interview.setStatus(InterviewStatus.PROPOSED);
        interview.setCandidatePreferredAt(null);
        Interview saved = interviewRepository.save(interview);

        notifyCandidate(interview.getApplication(), "Your interview for '" + interview.getApplication().getVacancy().getTitle()
                + "' has been rescheduled to " + request.getScheduledAt() + ". Please confirm.");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public InterviewResponse cancel(Long recruiterId, Long interviewId) {
        Interview interview = findOwnedInterview(recruiterId, interviewId);
        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new ValidationException("Interview is already cancelled");
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        Interview saved = interviewRepository.save(interview);

        notifyCandidate(interview.getApplication(),
                "Your interview for '" + interview.getApplication().getVacancy().getTitle() + "' has been cancelled.");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public InterviewResponse accept(Long jobSeekerId, Long interviewId) {
        Interview interview = findOwnedInterviewForCandidate(jobSeekerId, interviewId);
        if (!ACCEPTABLE_FROM.contains(interview.getStatus())) {
            throw new ValidationException("This interview cannot be accepted in its current state");
        }

        interview.setStatus(InterviewStatus.CONFIRMED);
        interview.setCandidatePreferredAt(null);
        Interview saved = interviewRepository.save(interview);

        Long recruiterId = interview.getApplication().getVacancy().getPostedBy().getId();
        notificationService.notify(recruiterId, "INTERVIEW_CONFIRMED",
                "The candidate confirmed the interview for '" + interview.getApplication().getVacancy().getTitle()
                        + "' on " + interview.getScheduledAt() + ".");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public InterviewResponse requestReschedule(Long jobSeekerId, Long interviewId, CandidateRescheduleRequest request) {
        Interview interview = findOwnedInterviewForCandidate(jobSeekerId, interviewId);
        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new ValidationException("A cancelled interview cannot be rescheduled");
        }

        interview.setCandidatePreferredAt(request.getPreferredAt());
        interview.setStatus(InterviewStatus.RESCHEDULE_REQUESTED);
        Interview saved = interviewRepository.save(interview);

        Long recruiterId = interview.getApplication().getVacancy().getPostedBy().getId();
        String message = "The candidate requested to reschedule the interview for '"
                + interview.getApplication().getVacancy().getTitle() + "' to " + request.getPreferredAt() + "."
                + (request.getReason() != null ? " Reason: " + request.getReason() : "");
        notificationService.notify(recruiterId, "INTERVIEW_RESCHEDULE_REQUESTED", message);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getMyInterviews(Long jobSeekerId) {
        return interviewRepository.findByApplication_JobSeeker_IdOrderByScheduledAtDesc(jobSeekerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsForApplication(Long recruiterId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (!application.getVacancy().getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You do not own this vacancy");
        }
        return interviewRepository.findByApplicationIdOrderByScheduledAtDesc(applicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getMyPanelInterviews(Long panelMemberId) {
        return interviewRepository.findByPanelMembers_IdOrderByScheduledAtAsc(panelMemberId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PanelMemberSummaryResponse> listPanelMembers() {
        return interviewPanelMemberRepository.findAll().stream()
                .map(pm -> PanelMemberSummaryResponse.builder().id(pm.getId()).name(pm.getName()).email(pm.getEmail()).build())
                .toList();
    }

    @Override
    @Transactional
    public InterviewFeedbackResponse submitFeedback(Long panelMemberId, Long interviewId, SubmitFeedbackRequest request) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + interviewId));

        boolean isAssigned = interview.getPanelMembers().stream().anyMatch(pm -> pm.getId().equals(panelMemberId));
        if (!isAssigned) {
            throw new UnauthorizedException("You are not assigned to this interview");
        }

        InterviewFeedback feedback = interviewFeedbackRepository.findByInterviewIdAndPanelMemberId(interviewId, panelMemberId)
                .orElseGet(() -> InterviewFeedback.builder()
                        .interview(interview)
                        .panelMember(interviewPanelMemberRepository.findById(panelMemberId)
                                .orElseThrow(() -> new ResourceNotFoundException("Panel member not found: " + panelMemberId)))
                        .build());
        feedback.setScore(request.getScore());
        feedback.setComments(request.getComments());
        InterviewFeedback saved = interviewFeedbackRepository.save(feedback);

        if (interviewFeedbackRepository.countByInterviewId(interviewId) >= interview.getPanelMembers().size()) {
            applicationService.markAssessed(interview.getApplication().getId());
        }

        return toFeedbackResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewFeedbackResponse> getFeedbackForInterview(Long recruiterId, Long interviewId) {
        Interview interview = findOwnedInterview(recruiterId, interviewId);
        return interviewFeedbackRepository.findByInterviewId(interview.getId()).stream()
                .map(this::toFeedbackResponse)
                .toList();
    }

    private Interview findOwnedInterview(Long recruiterId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + interviewId));
        if (!interview.getApplication().getVacancy().getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You do not own this interview");
        }
        return interview;
    }

    private Interview findOwnedInterviewForCandidate(Long jobSeekerId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + interviewId));
        if (!interview.getApplication().getJobSeeker().getId().equals(jobSeekerId)) {
            throw new UnauthorizedException("This interview does not belong to you");
        }
        return interview;
    }

    private void notifyCandidate(Application application, String message) {
        notificationService.notify(application.getJobSeeker().getId(), "INTERVIEW_UPDATE", message);
    }

    private InterviewResponse toResponse(Interview interview) {
        Application application = interview.getApplication();
        List<PanelMemberSummaryResponse> panelMembers = interview.getPanelMembers().stream()
                .map(pm -> PanelMemberSummaryResponse.builder().id(pm.getId()).name(pm.getName()).email(pm.getEmail()).build())
                .toList();

        return InterviewResponse.builder()
                .id(interview.getId())
                .applicationId(application.getId())
                .vacancyTitle(application.getVacancy().getTitle())
                .candidateName(application.getJobSeeker().getName())
                .scheduledAt(interview.getScheduledAt())
                .location(interview.getLocation())
                .status(interview.getStatus())
                .candidatePreferredAt(interview.getCandidatePreferredAt())
                .panelMembers(panelMembers)
                .panelMemberCount(panelMembers.size())
                .feedbackSubmittedCount((int) interviewFeedbackRepository.countByInterviewId(interview.getId()))
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }

    private InterviewFeedbackResponse toFeedbackResponse(InterviewFeedback feedback) {
        return InterviewFeedbackResponse.builder()
                .id(feedback.getId())
                .interviewId(feedback.getInterview().getId())
                .panelMemberId(feedback.getPanelMember().getId())
                .panelMemberName(feedback.getPanelMember().getName())
                .score(feedback.getScore())
                .comments(feedback.getComments())
                .submittedAt(feedback.getSubmittedAt())
                .build();
    }
}
