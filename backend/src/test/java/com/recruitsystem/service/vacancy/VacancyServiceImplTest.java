package com.recruitsystem.service.vacancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.recruitsystem.dto.vacancy.VacancyRequest;
import com.recruitsystem.dto.vacancy.VacancyResponse;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.company.ClientCompany;
import com.recruitsystem.entity.vacancy.JobVacancy;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.vacancy.JobVacancyRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VacancyServiceImplTest {

    @Mock
    private JobVacancyRepository jobVacancyRepository;
    @Mock
    private UserRepository userRepository;

    private VacancyServiceImpl vacancyService;

    @BeforeEach
    void setUp() {
        vacancyService = new VacancyServiceImpl(jobVacancyRepository, userRepository);
    }

    private Recruiter recruiter(Long id) {
        return Recruiter.builder()
                .id(id)
                .name("Rita Recruiter")
                .email("rita@example.com")
                .passwordHash("hashed")
                .accountStatus(AccountStatus.ACTIVE)
                .clientCompany(company())
                .build();
    }

    private ClientCompany company() {
        return ClientCompany.builder().id(10L).companyName("Acme Corp").build();
    }

    private VacancyRequest request() {
        VacancyRequest request = new VacancyRequest();
        request.setTitle("Backend Engineer");
        request.setDescription("Build things.");
        request.setRequirements("Java, Spring.");
        request.setCategory(VacancyCategory.IT);
        request.setLocation("Colombo");
        request.setDeadline(LocalDate.now().plusDays(30));
        return request;
    }

    private JobVacancy vacancy(Long id, Long posterId, VacancyStatus status) {
        return JobVacancy.builder()
                .id(id)
                .title("Backend Engineer")
                .description("Build things.")
                .requirements("Java, Spring.")
                .category(VacancyCategory.IT)
                .deadline(LocalDate.now().plusDays(30))
                .status(status)
                .postedBy(recruiter(posterId))
                .clientCompany(company())
                .build();
    }

    @Test
    void createVacancy_startsAsDraftAndUsesPostersCompany() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(recruiter(1L)));
        when(jobVacancyRepository.save(any())).thenAnswer(invocation -> {
            JobVacancy vacancy = invocation.getArgument(0);
            vacancy.setId(100L);
            return vacancy;
        });

        VacancyResponse response = vacancyService.createVacancy(1L, request());

        assertThat(response.getStatus()).isEqualTo(VacancyStatus.DRAFT);
        assertThat(response.getClientCompanyId()).isEqualTo(10L);
    }

    @Test
    void createVacancy_throwsWhenPosterHasNoClientCompany() {
        Recruiter recruiterWithoutCompany = Recruiter.builder()
                .id(1L)
                .name("Rita Recruiter")
                .email("rita@example.com")
                .passwordHash("hashed")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(recruiterWithoutCompany));

        assertThatThrownBy(() -> vacancyService.createVacancy(1L, request()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateVacancy_throwsWhenCallerIsNotThePoster() {
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(vacancy(5L, 1L, VacancyStatus.DRAFT)));

        assertThatThrownBy(() -> vacancyService.updateVacancy(2L, 5L, request()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateVacancy_throwsWhenVacancyIsClosed() {
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(vacancy(5L, 1L, VacancyStatus.CLOSED)));

        assertThatThrownBy(() -> vacancyService.updateVacancy(1L, 5L, request()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void publishVacancy_throwsWhenNotDraft() {
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(vacancy(5L, 1L, VacancyStatus.PUBLISHED)));

        assertThatThrownBy(() -> vacancyService.publishVacancy(1L, 5L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void publishVacancy_setsPublishedAtWhenDraft() {
        JobVacancy draft = vacancy(5L, 1L, VacancyStatus.DRAFT);
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(draft));
        when(jobVacancyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VacancyResponse response = vacancyService.publishVacancy(1L, 5L);

        assertThat(response.getStatus()).isEqualTo(VacancyStatus.PUBLISHED);
        assertThat(response.getPublishedAt()).isNotNull();
    }

    @Test
    void closeVacancy_throwsWhenAlreadyClosed() {
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(vacancy(5L, 1L, VacancyStatus.CLOSED)));

        assertThatThrownBy(() -> vacancyService.closeVacancy(1L, 5L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getVacancy_hidesDraftFromNonOwner() {
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(vacancy(5L, 1L, VacancyStatus.DRAFT)));

        assertThatThrownBy(() -> vacancyService.getVacancy(2L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getVacancy_visibleToOwnerEvenAsDraft() {
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(vacancy(5L, 1L, VacancyStatus.DRAFT)));

        VacancyResponse response = vacancyService.getVacancy(1L, 5L);

        assertThat(response.getId()).isEqualTo(5L);
    }

    @Test
    void getVacancy_visibleToAnyoneWhenPublished() {
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(vacancy(5L, 1L, VacancyStatus.PUBLISHED)));

        VacancyResponse response = vacancyService.getVacancy(999L, 5L);

        assertThat(response.getId()).isEqualTo(5L);
    }

    @Test
    void getMyVacancies_returnsOwnedVacancies() {
        when(jobVacancyRepository.findByPostedByIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(vacancy(5L, 1L, VacancyStatus.DRAFT), vacancy(6L, 1L, VacancyStatus.PUBLISHED)));

        List<VacancyResponse> responses = vacancyService.getMyVacancies(1L);

        assertThat(responses).hasSize(2);
    }

    @Test
    void search_passesBlankFiltersAsNull() {
        when(jobVacancyRepository.search(null, VacancyCategory.IT, null, null))
                .thenReturn(List.of(vacancy(6L, 1L, VacancyStatus.PUBLISHED)));

        List<VacancyResponse> responses = vacancyService.search("  ", VacancyCategory.IT, "", null);

        assertThat(responses).hasSize(1);
    }

    @Test
    void search_returnsMappedResults() {
        when(jobVacancyRepository.search("engineer", null, "Colombo", null))
                .thenReturn(List.of(vacancy(6L, 1L, VacancyStatus.PUBLISHED)));

        List<VacancyResponse> responses = vacancyService.search("engineer", null, "Colombo", null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Backend Engineer");
    }
}
