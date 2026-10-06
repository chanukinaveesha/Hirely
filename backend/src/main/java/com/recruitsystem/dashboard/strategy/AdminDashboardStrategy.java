package com.recruitsystem.dashboard.strategy;

import com.recruitsystem.dashboard.dto.AdminDashboardSection;
import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.repository.auth.HrExecutiveRepository;
import com.recruitsystem.repository.auth.InterviewPanelMemberRepository;
import com.recruitsystem.repository.auth.JobSeekerRepository;
import com.recruitsystem.repository.auth.RecruiterRepository;
import com.recruitsystem.repository.auth.SystemAdministratorRepository;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.vacancy.JobVacancyRepository;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Strategy Pattern - Concrete Strategy for the SYSTEM_ADMINISTRATOR role.
 */
@Component
@RequiredArgsConstructor
public class AdminDashboardStrategy implements DashboardStrategy {

    private final UserRepository userRepository;
    private final JobVacancyRepository jobVacancyRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final RecruiterRepository recruiterRepository;
    private final HrExecutiveRepository hrExecutiveRepository;
    private final InterviewPanelMemberRepository interviewPanelMemberRepository;
    private final SystemAdministratorRepository systemAdministratorRepository;

    @Override
    public UserRole getRole() {
        return UserRole.SYSTEM_ADMINISTRATOR;
    }

    @Override
    public DashboardSection buildSection(Long userId) {
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
