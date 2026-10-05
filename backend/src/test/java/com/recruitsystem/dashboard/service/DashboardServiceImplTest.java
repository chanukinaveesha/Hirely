package com.recruitsystem.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.recruitsystem.dashboard.dto.DashboardResponse;
import com.recruitsystem.dashboard.repository.DashboardApplicationRepository;
import com.recruitsystem.dashboard.repository.DashboardCandidateAssessmentRepository;
import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.dto.resume.ResumeResponse;
import com.recruitsystem.dto.vacancy.VacancyResponse;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.assessment.CandidateAssessmentStatus;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.post.service.PostService;
import com.recruitsystem.repository.auth.HrExecutiveRepository;
import com.recruitsystem.repository.auth.InterviewPanelMemberRepository;
import com.recruitsystem.repository.auth.JobSeekerRepository;
import com.recruitsystem.repository.auth.RecruiterRepository;
import com.recruitsystem.repository.auth.SystemAdministratorRepository;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.vacancy.JobVacancyRepository;
import com.recruitsystem.service.application.ApplicationService;
import com.recruitsystem.service.assessment.AssessmentService;
import com.recruitsystem.service.interview.InterviewService;
import com.recruitsystem.service.resume.ResumeService;
import com.recruitsystem.service.vacancy.VacancyService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private ApplicationService applicationService;
    @Mock private InterviewService interviewService;
    @Mock private AssessmentService assessmentService;
    @Mock private ResumeService resumeService;
    @Mock private VacancyService vacancyService;
    @Mock private PostService postService;
    @Mock private UserRepository userRepository;
    @Mock private JobVacancyRepository jobVacancyRepository;
    @Mock private JobSeekerRepository jobSeekerRepository;
    @Mock private RecruiterRepository recruiterRepository;
    @Mock private HrExecutiveRepository hrExecutiveRepository;
    @Mock private InterviewPanelMemberRepository interviewPanelMemberRepository;
    @Mock private SystemAdministratorRepository systemAdministratorRepository;
    @Mock private DashboardCandidateAssessmentRepository dashboardCandidateAssessmentRepository;
    @Mock private DashboardApplicationRepository dashboardApplicationRepository;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                applicationService, interviewService, assessmentService, resumeService, vacancyService, postService,
                userRepository, jobVacancyRepository, jobSeekerRepository, recruiterRepository, hrExecutiveRepository,
                interviewPanelMemberRepository, systemAdministratorRepository,
                dashboardCandidateAssessmentRepository, dashboardApplicationRepository);
        when(postService.getRecent(anyInt())).thenReturn(List.of());
    }

    @Test
    void getDashboard_forJobSeeker_aggregatesApplicationsInterviewsAssessmentsAndResume() {
        when(applicationService.getMyApplications(1L)).thenReturn(List.of(
                ApplicationResponse.builder().id(1L).status(ApplicationStatus.SUBMITTED).build(),
                ApplicationResponse.builder().id(2L).status(ApplicationStatus.SHORTLISTED).build()));
        when(interviewService.getMyInterviews(1L)).thenReturn(List.of(
                InterviewResponse.builder().id(10L).scheduledAt(LocalDateTime.now().plusDays(1)).build(),
                InterviewResponse.builder().id(11L).scheduledAt(LocalDateTime.now().minusDays(1)).build()));
        when(assessmentService.getMyCandidateAssessments(1L)).thenReturn(List.of());
        when(resumeService.getMyResume(1L)).thenReturn(ResumeResponse.builder().originalFilename("resume.pdf").build());

        DashboardResponse response = dashboardService.getDashboard(1L, UserRole.JOB_SEEKER);

        assertThat(response.getCandidate()).isNotNull();
        assertThat(response.getCandidate().getApplicationCountsByStatus())
                .containsEntry(ApplicationStatus.SUBMITTED, 1L)
                .containsEntry(ApplicationStatus.SHORTLISTED, 1L);
        assertThat(response.getCandidate().getUpcomingInterviews()).hasSize(1);
        assertThat(response.getCandidate().isResumeOnFile()).isTrue();
        assertThat(response.getCandidate().getResumeFileName()).isEqualTo("resume.pdf");
        assertThat(response.getRecruiter()).isNull();
    }

    @Test
    void getDashboard_forJobSeekerWithNoResume_reportsResumeNotOnFile() {
        when(applicationService.getMyApplications(1L)).thenReturn(List.of());
        when(interviewService.getMyInterviews(1L)).thenReturn(List.of());
        when(assessmentService.getMyCandidateAssessments(1L)).thenReturn(List.of());
        when(resumeService.getMyResume(1L)).thenThrow(new ResourceNotFoundException("No resume on file"));

        DashboardResponse response = dashboardService.getDashboard(1L, UserRole.JOB_SEEKER);

        assertThat(response.getCandidate().isResumeOnFile()).isFalse();
        assertThat(response.getCandidate().getResumeFileName()).isNull();
    }

    @Test
    void getDashboard_forRecruiter_countsVacanciesAndApplicants() {
        VacancyResponse published = VacancyResponse.builder().id(100L).status(VacancyStatus.PUBLISHED).build();
        when(vacancyService.getMyVacancies(2L)).thenReturn(List.of(published));
        when(applicationService.getApplicantsForVacancy(2L, 100L)).thenReturn(List.of(
                ApplicationResponse.builder().id(1L).status(ApplicationStatus.UNDER_REVIEW).build(),
                ApplicationResponse.builder().id(2L).status(ApplicationStatus.SHORTLISTED).build(),
                ApplicationResponse.builder().id(3L).status(ApplicationStatus.REJECTED).build()));

        DashboardResponse response = dashboardService.getDashboard(2L, UserRole.RECRUITER);

        assertThat(response.getRecruiter()).isNotNull();
        assertThat(response.getRecruiter().getVacancyCountsByStatus()).containsEntry(VacancyStatus.PUBLISHED, 1L);
        assertThat(response.getRecruiter().getApplicantsAwaitingReview()).isEqualTo(1);
        assertThat(response.getRecruiter().getShortlistedCount()).isEqualTo(1);
    }

    @Test
    void getDashboard_forHrExecutive_delegatesToDashboardRepositories() {
        when(dashboardCandidateAssessmentRepository.countByAssessment_CreatedBy_IdAndStatus(3L, CandidateAssessmentStatus.SUBMITTED))
                .thenReturn(4L);
        when(dashboardApplicationRepository.countShortlistedAwaitingInterview(3L)).thenReturn(2L);

        DashboardResponse response = dashboardService.getDashboard(3L, UserRole.HR_EXECUTIVE);

        assertThat(response.getHr().getAssessmentsToEvaluate()).isEqualTo(4L);
        assertThat(response.getHr().getInterviewsToSchedule()).isEqualTo(2L);
    }

    @Test
    void getDashboard_forPanelMember_filtersToFutureInterviews() {
        when(interviewService.getMyPanelInterviews(4L)).thenReturn(List.of(
                InterviewResponse.builder().id(10L).scheduledAt(LocalDateTime.now().plusDays(1)).build(),
                InterviewResponse.builder().id(11L).scheduledAt(LocalDateTime.now().minusDays(1)).build()));

        DashboardResponse response = dashboardService.getDashboard(4L, UserRole.INTERVIEW_PANEL_MEMBER);

        assertThat(response.getPanel().getUpcomingInterviews()).hasSize(1);
        assertThat(response.getPanel().getUpcomingInterviews().get(0).getId()).isEqualTo(10L);
    }

    @Test
    void getDashboard_forAdmin_aggregatesTotals() {
        when(userRepository.count()).thenReturn(50L);
        when(jobVacancyRepository.count()).thenReturn(12L);
        when(jobSeekerRepository.count()).thenReturn(30L);
        when(recruiterRepository.count()).thenReturn(10L);
        when(hrExecutiveRepository.count()).thenReturn(5L);
        when(interviewPanelMemberRepository.count()).thenReturn(4L);
        when(systemAdministratorRepository.count()).thenReturn(1L);

        DashboardResponse response = dashboardService.getDashboard(5L, UserRole.SYSTEM_ADMINISTRATOR);

        assertThat(response.getAdmin().getTotalUsers()).isEqualTo(50L);
        assertThat(response.getAdmin().getTotalVacancies()).isEqualTo(12L);
        assertThat(response.getAdmin().getUsersByRole()).containsEntry(UserRole.JOB_SEEKER, 30L);
    }
}
