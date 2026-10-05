package com.recruitsystem.dashboard.service;

import com.recruitsystem.dashboard.dto.AdminDashboardSection;
import com.recruitsystem.dashboard.dto.CandidateDashboardSection;
import com.recruitsystem.dashboard.dto.DashboardResponse;
import com.recruitsystem.dashboard.dto.HrDashboardSection;
import com.recruitsystem.dashboard.dto.PanelDashboardSection;
import com.recruitsystem.dashboard.dto.RecruiterDashboardSection;
import com.recruitsystem.dashboard.repository.DashboardApplicationRepository;
import com.recruitsystem.dashboard.repository.DashboardCandidateAssessmentRepository;
import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.assessment.CandidateAssessmentResponse;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.dto.resume.ResumeResponse;
import com.recruitsystem.dto.vacancy.VacancyResponse;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.assessment.CandidateAssessmentStatus;
import com.recruitsystem.entity.auth.UserRole;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_LIST_LIMIT = 5;
    private static final int RECENT_POSTS_LIMIT = 5;
    private static final Set<ApplicationStatus> AWAITING_REVIEW_STATUSES =
            Set.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW);

    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final AssessmentService assessmentService;
    private final ResumeService resumeService;
    private final VacancyService vacancyService;
    private final PostService postService;

    private final UserRepository userRepository;
    private final JobVacancyRepository jobVacancyRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final RecruiterRepository recruiterRepository;
    private final HrExecutiveRepository hrExecutiveRepository;
    private final InterviewPanelMemberRepository interviewPanelMemberRepository;
    private final SystemAdministratorRepository systemAdministratorRepository;

    private final DashboardCandidateAssessmentRepository dashboardCandidateAssessmentRepository;
    private final DashboardApplicationRepository dashboardApplicationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId, UserRole role) {
        DashboardResponse.DashboardResponseBuilder response = DashboardResponse.builder()
                .recentPosts(postService.getRecent(RECENT_POSTS_LIMIT));

        switch (role) {
            case JOB_SEEKER -> response.candidate(buildCandidateSection(userId));
            case RECRUITER -> response.recruiter(buildRecruiterSection(userId));
            case HR_EXECUTIVE -> response.hr(buildHrSection(userId));
            case INTERVIEW_PANEL_MEMBER -> response.panel(buildPanelSection(userId));
            case SYSTEM_ADMINISTRATOR -> response.admin(buildAdminSection());
        }

        return response.build();
    }

    private CandidateDashboardSection buildCandidateSection(Long jobSeekerId) {
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

    private RecruiterDashboardSection buildRecruiterSection(Long recruiterId) {
        List<VacancyResponse> myVacancies = vacancyService.getMyVacancies(recruiterId);
        Map<com.recruitsystem.entity.vacancy.VacancyStatus, Long> vacancyCountsByStatus = myVacancies.stream()
                .collect(Collectors.groupingBy(VacancyResponse::getStatus, Collectors.counting()));

        long awaitingReview = 0;
        long shortlisted = 0;
        for (VacancyResponse vacancy : myVacancies) {
            List<ApplicationResponse> applicants = applicationService.getApplicantsForVacancy(recruiterId, vacancy.getId());
            for (ApplicationResponse applicant : applicants) {
                if (AWAITING_REVIEW_STATUSES.contains(applicant.getStatus())) {
                    awaitingReview++;
                } else if (applicant.getStatus() == ApplicationStatus.SHORTLISTED) {
                    shortlisted++;
                }
            }
        }

        return RecruiterDashboardSection.builder()
                .vacancyCountsByStatus(vacancyCountsByStatus)
                .applicantsAwaitingReview(awaitingReview)
                .shortlistedCount(shortlisted)
                .build();
    }

    private HrDashboardSection buildHrSection(Long hrUserId) {
        return HrDashboardSection.builder()
                .assessmentsToEvaluate(dashboardCandidateAssessmentRepository
                        .countByAssessment_CreatedBy_IdAndStatus(hrUserId, CandidateAssessmentStatus.SUBMITTED))
                .interviewsToSchedule(dashboardApplicationRepository.countShortlistedAwaitingInterview(hrUserId))
                .build();
    }

    private PanelDashboardSection buildPanelSection(Long panelMemberId) {
        List<InterviewResponse> upcomingInterviews = interviewService.getMyPanelInterviews(panelMemberId).stream()
                .filter(i -> i.getScheduledAt() != null && i.getScheduledAt().isAfter(LocalDateTime.now()))
                .limit(RECENT_LIST_LIMIT)
                .toList();

        return PanelDashboardSection.builder().upcomingInterviews(upcomingInterviews).build();
    }

    private AdminDashboardSection buildAdminSection() {
        Map<UserRole, Long> usersByRole = new EnumMap<>(UserRole.class);
        usersByRole.put(UserRole.JOB_SEEKER, jobSeekerRepository.count());
        usersByRole.put(UserRole.RECRUITER, recruiterRepository.count());
        usersByRole.put(UserRole.HR_EXECUTIVE, hrExecutiveRepository.count());
        usersByRole.put(UserRole.INTERVIEW_PANEL_MEMBER, interviewPanelMemberRepository.count());
        usersByRole.put(UserRole.SYSTEM_ADMINISTRATOR, systemAdministratorRepository.count());

        return AdminDashboardSection.builder()
                .totalUsers(userRepository.count())
                .totalVacancies(jobVacancyRepository.count())
                .usersByRole(usersByRole)
                .build();
    }
}
