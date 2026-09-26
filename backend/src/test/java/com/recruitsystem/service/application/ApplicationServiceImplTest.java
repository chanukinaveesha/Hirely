package com.recruitsystem.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.application.ApplyToVacancyRequest;
import com.recruitsystem.entity.application.Application;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.company.ClientCompany;
import com.recruitsystem.entity.vacancy.JobVacancy;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.exception.DuplicateResourceException;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.application.ApplicationRepository;
import com.recruitsystem.repository.auth.JobSeekerRepository;
import com.recruitsystem.repository.vacancy.JobVacancyRepository;
import com.recruitsystem.service.notification.NotificationService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private JobSeekerRepository jobSeekerRepository;
    @Mock
    private JobVacancyRepository jobVacancyRepository;
    @Mock
    private NotificationService notificationService;

    private ApplicationServiceImpl applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationServiceImpl(
                applicationRepository, jobSeekerRepository, jobVacancyRepository, notificationService);
    }

    private JobSeeker jobSeeker() {
        return JobSeeker.builder()
                .id(1L)
                .name("Sam Seeker")
                .email("sam@example.com")
                .passwordHash("hashed")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    private JobVacancy vacancy(VacancyStatus status, LocalDate deadline) {
        ClientCompany company = ClientCompany.builder().id(10L).companyName("Acme Corp").build();
        Recruiter poster = Recruiter.builder().id(2L).name("Rita").email("rita@example.com")
                .passwordHash("hashed").accountStatus(AccountStatus.ACTIVE).clientCompany(company).build();
        return JobVacancy.builder()
                .id(5L)
                .title("Backend Engineer")
                .category(VacancyCategory.IT)
                .status(status)
                .deadline(deadline)
                .postedBy(poster)
                .clientCompany(company)
                .build();
    }

    private ApplyToVacancyRequest request(Long vacancyId) {
        ApplyToVacancyRequest request = new ApplyToVacancyRequest();
        request.setVacancyId(vacancyId);
        return request;
    }

    @Test
    void apply_succeedsForPublishedOpenVacancy() {
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker()));
        when(jobVacancyRepository.findById(5L))
                .thenReturn(Optional.of(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10))));
        when(applicationRepository.existsByJobSeekerIdAndVacancyId(1L, 5L)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application application = invocation.getArgument(0);
            application.setId(100L);
            return application;
        });

        ApplicationResponse response = applicationService.apply(1L, request(5L));

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(response.getVacancyTitle()).isEqualTo("Backend Engineer");
    }

    @Test
    void apply_throwsWhenVacancyNotPublished() {
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker()));
        when(jobVacancyRepository.findById(5L))
                .thenReturn(Optional.of(vacancy(VacancyStatus.DRAFT, LocalDate.now().plusDays(10))));

        assertThatThrownBy(() -> applicationService.apply(1L, request(5L)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void apply_throwsWhenDeadlinePassed() {
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker()));
        when(jobVacancyRepository.findById(5L))
                .thenReturn(Optional.of(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().minusDays(1))));

        assertThatThrownBy(() -> applicationService.apply(1L, request(5L)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void apply_throwsWhenAlreadyApplied() {
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker()));
        when(jobVacancyRepository.findById(5L))
                .thenReturn(Optional.of(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10))));
        when(applicationRepository.existsByJobSeekerIdAndVacancyId(1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply(1L, request(5L)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void withdraw_throwsWhenCallerDoesNotOwnApplication() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.SUBMITTED)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.withdraw(999L, 100L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void withdraw_throwsWhenAlreadyInTerminalStatus() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.SELECTED)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.withdraw(1L, 100L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void withdraw_setsStatusToWithdrawn() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.UNDER_REVIEW)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        applicationService.withdraw(1L, 100L);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    void apply_throwsWhenVacancyMissing() {
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker()));
        when(jobVacancyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.apply(1L, request(999L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getApplicantsForVacancy_throwsWhenCallerDoesNotOwnVacancy() {
        when(jobVacancyRepository.findById(5L))
                .thenReturn(Optional.of(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10))));

        assertThatThrownBy(() -> applicationService.getApplicantsForVacancy(999L, 5L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getApplicantsForVacancy_returnsApplicantsWithJobSeekerInfo() {
        when(jobVacancyRepository.findById(5L))
                .thenReturn(Optional.of(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10))));
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.SUBMITTED)
                .build();
        when(applicationRepository.findByVacancyIdOrderByAppliedAtDesc(5L)).thenReturn(List.of(application));

        List<ApplicationResponse> responses = applicationService.getApplicantsForVacancy(2L, 5L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getJobSeekerName()).isEqualTo("Sam Seeker");
    }

    @Test
    void shortlist_throwsWhenNotSubmittedOrUnderReview() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.SHORTLISTED)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.shortlist(2L, 100L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shortlist_throwsWhenCallerDoesNotOwnVacancy() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.SUBMITTED)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.shortlist(999L, 100L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void shortlist_setsStatusAndNotifiesCandidate() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.SUBMITTED)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = applicationService.shortlist(2L, 100L);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
        verify(notificationService).notify(eq(1L), anyString(), anyString());
    }

    @Test
    void reject_throwsWhenAlreadyInTerminalStatus() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.SELECTED)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.reject(2L, 100L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void reject_allowedFromShortlisted() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.SHORTLISTED)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = applicationService.reject(2L, 100L);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        verify(notificationService).notify(eq(1L), anyString(), anyString());
    }

    @Test
    void select_throwsWhenNotShortlistedOrLater() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.SUBMITTED)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.select(2L, 100L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void select_allowedFromAssessedAndNotifiesCandidate() {
        Application application = Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy(VacancyStatus.PUBLISHED, LocalDate.now().plusDays(10)))
                .status(ApplicationStatus.ASSESSED)
                .build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = applicationService.select(2L, 100L);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.SELECTED);
        verify(notificationService).notify(eq(1L), anyString(), anyString());
    }

    @Test
    void markInterviewing_onlyTransitionsFromShortlisted() {
        Application shortlisted = Application.builder().id(100L).status(ApplicationStatus.SHORTLISTED).build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(shortlisted));

        applicationService.markInterviewing(100L);

        assertThat(shortlisted.getStatus()).isEqualTo(ApplicationStatus.INTERVIEWING);
    }

    @Test
    void markInterviewing_noOpWhenNotShortlisted() {
        Application submitted = Application.builder().id(100L).status(ApplicationStatus.SUBMITTED).build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(submitted));

        applicationService.markInterviewing(100L);

        assertThat(submitted.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
    }

    @Test
    void markAssessed_onlyTransitionsFromInterviewing() {
        Application interviewing = Application.builder().id(100L).status(ApplicationStatus.INTERVIEWING).build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(interviewing));

        applicationService.markAssessed(100L);

        assertThat(interviewing.getStatus()).isEqualTo(ApplicationStatus.ASSESSED);
    }
}
