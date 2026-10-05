package com.recruitsystem.dashboard.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.recruitsystem.dashboard.dto.HrDashboardSection;
import com.recruitsystem.dashboard.repository.DashboardApplicationRepository;
import com.recruitsystem.dashboard.repository.DashboardCandidateAssessmentRepository;
import com.recruitsystem.entity.assessment.CandidateAssessmentStatus;
import com.recruitsystem.entity.auth.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HrDashboardStrategyTest {

    @Mock private DashboardCandidateAssessmentRepository dashboardCandidateAssessmentRepository;
    @Mock private DashboardApplicationRepository dashboardApplicationRepository;

    private HrDashboardStrategy strategy;

    @Test
    void getRole_returnsHrExecutive() {
        strategy = new HrDashboardStrategy(dashboardCandidateAssessmentRepository, dashboardApplicationRepository);
        assertThat(strategy.getRole()).isEqualTo(UserRole.HR_EXECUTIVE);
    }

    @Test
    void buildSection_delegatesToDashboardRepositories() {
        strategy = new HrDashboardStrategy(dashboardCandidateAssessmentRepository, dashboardApplicationRepository);
        when(dashboardCandidateAssessmentRepository.countByAssessment_CreatedBy_IdAndStatus(3L, CandidateAssessmentStatus.SUBMITTED))
                .thenReturn(4L);
        when(dashboardApplicationRepository.countShortlistedAwaitingInterview(3L)).thenReturn(2L);

        HrDashboardSection section = (HrDashboardSection) strategy.buildSection(3L);

        assertThat(section.getAssessmentsToEvaluate()).isEqualTo(4L);
        assertThat(section.getInterviewsToSchedule()).isEqualTo(2L);
    }
}
