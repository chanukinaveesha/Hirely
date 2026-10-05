package com.recruitsystem.dashboard.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.recruitsystem.dashboard.dto.CandidateDashboardSection;
import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.dto.resume.ResumeResponse;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.service.application.ApplicationService;
import com.recruitsystem.service.assessment.AssessmentService;
import com.recruitsystem.service.interview.InterviewService;
import com.recruitsystem.service.resume.ResumeService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CandidateDashboardStrategyTest {

    @Mock private ApplicationService applicationService;
    @Mock private InterviewService interviewService;
    @Mock private AssessmentService assessmentService;
    @Mock private ResumeService resumeService;

    private CandidateDashboardStrategy strategy;

    @Test
    void getRole_returnsJobSeeker() {
        strategy = new CandidateDashboardStrategy(applicationService, interviewService, assessmentService, resumeService);
        assertThat(strategy.getRole()).isEqualTo(UserRole.JOB_SEEKER);
    }

    @Test
    void buildSection_aggregatesApplicationsInterviewsAssessmentsAndResume() {
        strategy = new CandidateDashboardStrategy(applicationService, interviewService, assessmentService, resumeService);
        when(applicationService.getMyApplications(1L)).thenReturn(List.of(
                ApplicationResponse.builder().id(1L).status(ApplicationStatus.SUBMITTED).build(),
                ApplicationResponse.builder().id(2L).status(ApplicationStatus.SHORTLISTED).build()));
        when(interviewService.getMyInterviews(1L)).thenReturn(List.of(
                InterviewResponse.builder().id(10L).scheduledAt(LocalDateTime.now().plusDays(1)).build(),
                InterviewResponse.builder().id(11L).scheduledAt(LocalDateTime.now().minusDays(1)).build()));
        when(assessmentService.getMyCandidateAssessments(1L)).thenReturn(List.of());
        when(resumeService.getMyResume(1L)).thenReturn(ResumeResponse.builder().originalFilename("resume.pdf").build());

        DashboardSection result = strategy.buildSection(1L);

        assertThat(result).isInstanceOf(CandidateDashboardSection.class);
        CandidateDashboardSection section = (CandidateDashboardSection) result;
        assertThat(section.getApplicationCountsByStatus())
                .containsEntry(ApplicationStatus.SUBMITTED, 1L)
                .containsEntry(ApplicationStatus.SHORTLISTED, 1L);
        assertThat(section.getUpcomingInterviews()).hasSize(1);
        assertThat(section.isResumeOnFile()).isTrue();
        assertThat(section.getResumeFileName()).isEqualTo("resume.pdf");
    }

    @Test
    void buildSection_withNoResume_reportsResumeNotOnFile() {
        strategy = new CandidateDashboardStrategy(applicationService, interviewService, assessmentService, resumeService);
        when(applicationService.getMyApplications(1L)).thenReturn(List.of());
        when(interviewService.getMyInterviews(1L)).thenReturn(List.of());
        when(assessmentService.getMyCandidateAssessments(1L)).thenReturn(List.of());
        when(resumeService.getMyResume(1L)).thenThrow(new ResourceNotFoundException("No resume on file"));

        CandidateDashboardSection section = (CandidateDashboardSection) strategy.buildSection(1L);

        assertThat(section.isResumeOnFile()).isFalse();
        assertThat(section.getResumeFileName()).isNull();
    }
}
