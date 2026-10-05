package com.recruitsystem.dashboard.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.recruitsystem.dashboard.dto.AdminDashboardSection;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.repository.auth.HrExecutiveRepository;
import com.recruitsystem.repository.auth.InterviewPanelMemberRepository;
import com.recruitsystem.repository.auth.JobSeekerRepository;
import com.recruitsystem.repository.auth.RecruiterRepository;
import com.recruitsystem.repository.auth.SystemAdministratorRepository;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.vacancy.JobVacancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDashboardStrategyTest {

    @Mock private UserRepository userRepository;
    @Mock private JobVacancyRepository jobVacancyRepository;
    @Mock private JobSeekerRepository jobSeekerRepository;
    @Mock private RecruiterRepository recruiterRepository;
    @Mock private HrExecutiveRepository hrExecutiveRepository;
    @Mock private InterviewPanelMemberRepository interviewPanelMemberRepository;
    @Mock private SystemAdministratorRepository systemAdministratorRepository;

    private AdminDashboardStrategy strategy;

    @Test
    void getRole_returnsSystemAdministrator() {
        strategy = new AdminDashboardStrategy(userRepository, jobVacancyRepository, jobSeekerRepository,
                recruiterRepository, hrExecutiveRepository, interviewPanelMemberRepository, systemAdministratorRepository);

        assertThat(strategy.getRole()).isEqualTo(UserRole.SYSTEM_ADMINISTRATOR);
    }

    @Test
    void buildSection_aggregatesTotals() {
        strategy = new AdminDashboardStrategy(userRepository, jobVacancyRepository, jobSeekerRepository,
                recruiterRepository, hrExecutiveRepository, interviewPanelMemberRepository, systemAdministratorRepository);
        when(userRepository.count()).thenReturn(50L);
        when(jobVacancyRepository.count()).thenReturn(12L);
        when(jobSeekerRepository.count()).thenReturn(30L);
        when(recruiterRepository.count()).thenReturn(10L);
        when(hrExecutiveRepository.count()).thenReturn(5L);
        when(interviewPanelMemberRepository.count()).thenReturn(4L);
        when(systemAdministratorRepository.count()).thenReturn(1L);

        AdminDashboardSection section = (AdminDashboardSection) strategy.buildSection(5L);

        assertThat(section.getTotalUsers()).isEqualTo(50L);
        assertThat(section.getTotalVacancies()).isEqualTo(12L);
        assertThat(section.getUsersByRole()).containsEntry(UserRole.JOB_SEEKER, 30L);
    }
}
