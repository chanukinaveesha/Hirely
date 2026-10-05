package com.recruitsystem.dashboard.strategy;

import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.dashboard.dto.HrDashboardSection;
import com.recruitsystem.dashboard.repository.DashboardApplicationRepository;
import com.recruitsystem.dashboard.repository.DashboardCandidateAssessmentRepository;
import com.recruitsystem.entity.assessment.CandidateAssessmentStatus;
import com.recruitsystem.entity.auth.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Strategy Pattern - Concrete Strategy for the HR_EXECUTIVE role.
 */
@Component
@RequiredArgsConstructor
public class HrDashboardStrategy implements DashboardStrategy {

    private final DashboardCandidateAssessmentRepository dashboardCandidateAssessmentRepository;
    private final DashboardApplicationRepository dashboardApplicationRepository;

    @Override
    public UserRole getRole() {
        return UserRole.HR_EXECUTIVE;
    }

    @Override
    public DashboardSection buildSection(Long hrUserId) {
        return HrDashboardSection.builder()
                .assessmentsToEvaluate(dashboardCandidateAssessmentRepository
                        .countByAssessment_CreatedBy_IdAndStatus(hrUserId, CandidateAssessmentStatus.SUBMITTED))
                .interviewsToSchedule(dashboardApplicationRepository.countShortlistedAwaitingInterview(hrUserId))
                .build();
    }
}
