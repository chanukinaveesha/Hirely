package com.recruitsystem.dashboard.strategy;

import com.recruitsystem.dashboard.dto.CandidateDashboardSection;
import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.assessment.CandidateAssessmentResponse;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.dto.resume.ResumeResponse;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.assessment.CandidateAssessmentStatus;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.service.application.ApplicationService;
import com.recruitsystem.service.assessment.AssessmentService;
import com.recruitsystem.service.interview.InterviewService;
import com.recruitsystem.service.resume.ResumeService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Strategy Pattern - Concrete Strategy for the JOB_SEEKER role.
 */
@Component
@RequiredArgsConstructor
public class CandidateDashboardStrategy implements DashboardStrategy {

    private static final int RECENT_LIST_LIMIT = 5;

    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final AssessmentService assessmentService;
    private final ResumeService resumeService;

    @Override
    public UserRole getRole() {
        return UserRole.JOB_SEEKER;
    }

    @Override
    public DashboardSection buildSection(Long jobSeekerId) {
        List<ApplicationResponse> applications = applicationService.getMyApplications(jobSeekerId);
        Map<ApplicationStatus, Long> countsByStatus = applications.stream()
                .collect(Collectors.groupingBy(ApplicationResponse::getStatus, Collectors.counting()));

        List<InterviewResponse> upcomingInterviews = interviewService.getMyInterviews(jobSeekerId).stream()
                .filter(i -> i.getScheduledAt() != null && i.getScheduledAt().isAfter(LocalDateTime.now()))
                .limit(RECENT_LIST_LIMIT)
                .toList();

        List<CandidateAssessmentResponse> assignedAssessments =
                assessmentService.getMyCandidateAssessments(jobSeekerId).stream()
                        .filter(a -> a.getStatus() == CandidateAssessmentStatus.ASSIGNED)
                        .limit(RECENT_LIST_LIMIT)
                        .toList();

        String resumeFileName = null;
        boolean resumeOnFile = true;
        try {
            ResumeResponse resume = resumeService.getMyResume(jobSeekerId);
            resumeFileName = resume.getOriginalFilename();
        } catch (ResourceNotFoundException ex) {
            resumeOnFile = false;
        }

        return CandidateDashboardSection.builder()
                .applicationCountsByStatus(countsByStatus)
                .upcomingInterviews(upcomingInterviews)
                .assignedAssessments(assignedAssessments)
                .resumeOnFile(resumeOnFile)
                .resumeFileName(resumeFileName)
                .build();
    }
}
