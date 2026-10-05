package com.recruitsystem.dashboard.strategy;

import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.dashboard.dto.RecruiterDashboardSection;
import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.vacancy.VacancyResponse;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.service.application.ApplicationService;
import com.recruitsystem.service.vacancy.VacancyService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Strategy Pattern - Concrete Strategy for the RECRUITER role.
 */
@Component
@RequiredArgsConstructor
public class RecruiterDashboardStrategy implements DashboardStrategy {

    private static final Set<ApplicationStatus> AWAITING_REVIEW_STATUSES =
            Set.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW);

    private final VacancyService vacancyService;
    private final ApplicationService applicationService;

    @Override
    public UserRole getRole() {
        return UserRole.RECRUITER;
    }

    @Override
    public DashboardSection buildSection(Long recruiterId) {
        List<VacancyResponse> myVacancies = vacancyService.getMyVacancies(recruiterId);
        Map<VacancyStatus, Long> vacancyCountsByStatus = myVacancies.stream()
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
}
