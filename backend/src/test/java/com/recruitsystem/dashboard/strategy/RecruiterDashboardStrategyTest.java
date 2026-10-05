package com.recruitsystem.dashboard.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.recruitsystem.dashboard.dto.RecruiterDashboardSection;
import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.vacancy.VacancyResponse;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.service.application.ApplicationService;
import com.recruitsystem.service.vacancy.VacancyService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecruiterDashboardStrategyTest {

    @Mock private VacancyService vacancyService;
    @Mock private ApplicationService applicationService;

    private RecruiterDashboardStrategy strategy;

    @Test
    void getRole_returnsRecruiter() {
        strategy = new RecruiterDashboardStrategy(vacancyService, applicationService);
        assertThat(strategy.getRole()).isEqualTo(UserRole.RECRUITER);
    }

    @Test
    void buildSection_countsVacanciesAndApplicants() {
        strategy = new RecruiterDashboardStrategy(vacancyService, applicationService);
        VacancyResponse published = VacancyResponse.builder().id(100L).status(VacancyStatus.PUBLISHED).build();
        when(vacancyService.getMyVacancies(2L)).thenReturn(List.of(published));
        when(applicationService.getApplicantsForVacancy(2L, 100L)).thenReturn(List.of(
                ApplicationResponse.builder().id(1L).status(ApplicationStatus.UNDER_REVIEW).build(),
                ApplicationResponse.builder().id(2L).status(ApplicationStatus.SHORTLISTED).build(),
                ApplicationResponse.builder().id(3L).status(ApplicationStatus.REJECTED).build()));

        RecruiterDashboardSection section = (RecruiterDashboardSection) strategy.buildSection(2L);

        assertThat(section.getVacancyCountsByStatus()).containsEntry(VacancyStatus.PUBLISHED, 1L);
        assertThat(section.getApplicantsAwaitingReview()).isEqualTo(1);
        assertThat(section.getShortlistedCount()).isEqualTo(1);
    }
}
