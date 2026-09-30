package com.recruitsystem.service.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recruitsystem.dto.assessment.AssessmentResponse;
import com.recruitsystem.dto.assessment.AssignAssessmentRequest;
import com.recruitsystem.dto.assessment.CandidateAssessmentResponse;
import com.recruitsystem.dto.assessment.CreateAssessmentRequest;
import com.recruitsystem.dto.assessment.EvaluateCandidateAssessmentRequest;
import com.recruitsystem.dto.assessment.SubmitCandidateAssessmentRequest;
import com.recruitsystem.entity.application.Application;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.assessment.Assessment;
import com.recruitsystem.entity.assessment.AssessmentType;
import com.recruitsystem.entity.assessment.CandidateAssessment;
import com.recruitsystem.entity.assessment.CandidateAssessmentStatus;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.HrExecutive;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.vacancy.JobVacancy;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.exception.DuplicateResourceException;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.application.ApplicationRepository;
import com.recruitsystem.repository.assessment.AssessmentRepository;
import com.recruitsystem.repository.assessment.CandidateAssessmentRepository;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.vacancy.JobVacancyRepository;
import com.recruitsystem.service.notification.NotificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceImplTest {

    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private CandidateAssessmentRepository candidateAssessmentRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private JobVacancyRepository jobVacancyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    private AssessmentServiceImpl assessmentService;

    @BeforeEach
    void setUp() {
        assessmentService = new AssessmentServiceImpl(
                assessmentRepository, candidateAssessmentRepository, applicationRepository,
                jobVacancyRepository, userRepository, notificationService);
    }

    private HrExecutive hrExecutive() {
        return HrExecutive.builder()
                .id(2L)
                .name("Hana Executive")
                .email("hana@example.com")
                .passwordHash("hashed")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
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

    private JobVacancy vacancy() {
        return JobVacancy.builder()
                .id(5L)
                .title("Backend Engineer")
                .category(VacancyCategory.IT)
                .status(VacancyStatus.PUBLISHED)
                .deadline(LocalDate.now().plusDays(10))
                .postedBy(hrExecutive())
                .build();
    }

    private Application application(ApplicationStatus status) {
        return Application.builder()
                .id(100L)
                .jobSeeker(jobSeeker())
                .vacancy(vacancy())
                .status(status)
                .build();
    }

    private Assessment assessment(boolean archived) {
        return Assessment.builder()
                .id(50L)
                .title("Aptitude Round 1")
                .description("General aptitude test")
                .type(AssessmentType.APTITUDE)
                .createdBy(hrExecutive())
                .archived(archived)
                .build();
    }

    private CandidateAssessment candidateAssessment(CandidateAssessmentStatus status, LocalDateTime deadline, Application application) {
        return CandidateAssessment.builder()
                .id(200L)
                .assessment(assessment(false))
                .application(application)
                .status(status)
                .deadline(deadline)
                .build();
    }

    @Test
    void createAssessment_savesWithCreator() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(hrExecutive()));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateAssessmentRequest request = new CreateAssessmentRequest();
        request.setTitle("Technical Round");
        request.setDescription("Coding test");
        request.setType(AssessmentType.TECHNICAL);

        AssessmentResponse response = assessmentService.createAssessment(2L, request);

        assertThat(response.getTitle()).isEqualTo("Technical Round");
        assertThat(response.getType()).isEqualTo(AssessmentType.TECHNICAL);
        assertThat(response.isArchived()).isFalse();
    }

    @Test
    void archiveAssessment_throwsWhenNotOwner() {
        when(assessmentRepository.findById(50L)).thenReturn(Optional.of(assessment(false)));

        assertThatThrownBy(() -> assessmentService.archiveAssessment(999L, 50L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void archiveAssessment_throwsWhenAlreadyArchived() {
        when(assessmentRepository.findById(50L)).thenReturn(Optional.of(assessment(true)));

        assertThatThrownBy(() -> assessmentService.archiveAssessment(2L, 50L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void archiveAssessment_setsArchivedTrue() {
        when(assessmentRepository.findById(50L)).thenReturn(Optional.of(assessment(false)));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentResponse response = assessmentService.archiveAssessment(2L, 50L);

        assertThat(response.isArchived()).isTrue();
    }

    @Test
    void assignAssessment_throwsWhenArchived() {
        when(assessmentRepository.findById(50L)).thenReturn(Optional.of(assessment(true)));

        AssignAssessmentRequest request = new AssignAssessmentRequest();
        request.setApplicationIds(List.of(100L));
        request.setDeadline(LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> assessmentService.assignAssessment(2L, 50L, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void assignAssessment_throwsWhenApplicationNotShortlistedOrLater() {
        when(assessmentRepository.findById(50L)).thenReturn(Optional.of(assessment(false)));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application(ApplicationStatus.SUBMITTED)));

        AssignAssessmentRequest request = new AssignAssessmentRequest();
        request.setApplicationIds(List.of(100L));
        request.setDeadline(LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> assessmentService.assignAssessment(2L, 50L, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void assignAssessment_throwsWhenCallerOwnsAssessmentButNotVacancy() {
        JobVacancy otherVacancy = JobVacancy.builder()
                .id(6L)
                .title("Frontend Engineer")
                .category(VacancyCategory.IT)
                .status(VacancyStatus.PUBLISHED)
                .deadline(LocalDate.now().plusDays(10))
                .postedBy(JobSeeker.builder().id(999L).name("Other Poster").email("other@example.com")
                        .passwordHash("hashed").accountStatus(AccountStatus.ACTIVE).build())
                .build();
        Application applicationWithOtherVacancy = Application.builder()
                .id(100L).jobSeeker(jobSeeker()).vacancy(otherVacancy).status(ApplicationStatus.SHORTLISTED).build();

        when(assessmentRepository.findById(50L)).thenReturn(Optional.of(assessment(false)));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(applicationWithOtherVacancy));

        AssignAssessmentRequest request = new AssignAssessmentRequest();
        request.setApplicationIds(List.of(100L));
        request.setDeadline(LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> assessmentService.assignAssessment(2L, 50L, request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void assignAssessment_throwsWhenCallerDoesNotOwnAssessment() {
        when(assessmentRepository.findById(50L)).thenReturn(Optional.of(assessment(false)));

        AssignAssessmentRequest request = new AssignAssessmentRequest();
        request.setApplicationIds(List.of(100L));
        request.setDeadline(LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> assessmentService.assignAssessment(999L, 50L, request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void assignAssessment_throwsOnDuplicateAssignment() {
        when(assessmentRepository.findById(50L)).thenReturn(Optional.of(assessment(false)));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application(ApplicationStatus.SHORTLISTED)));
        when(candidateAssessmentRepository.existsByAssessmentIdAndApplicationId(50L, 100L)).thenReturn(true);

        AssignAssessmentRequest request = new AssignAssessmentRequest();
        request.setApplicationIds(List.of(100L));
        request.setDeadline(LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> assessmentService.assignAssessment(2L, 50L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void assignAssessment_createsAssignmentAndNotifiesCandidate() {
        when(assessmentRepository.findById(50L)).thenReturn(Optional.of(assessment(false)));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application(ApplicationStatus.SHORTLISTED)));
        when(candidateAssessmentRepository.existsByAssessmentIdAndApplicationId(50L, 100L)).thenReturn(false);
        when(candidateAssessmentRepository.save(any(CandidateAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignAssessmentRequest request = new AssignAssessmentRequest();
        request.setApplicationIds(List.of(100L));
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);
        request.setDeadline(deadline);

        List<CandidateAssessmentResponse> responses = assessmentService.assignAssessment(2L, 50L, request);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(CandidateAssessmentStatus.ASSIGNED);
        assertThat(responses.get(0).getDeadline()).isEqualTo(deadline);
        verify(notificationService).notify(eq(1L), anyString(), anyString());
    }

    @Test
    void submitCandidateAssessment_throwsWhenNotOwner() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        CandidateAssessment candidateAssessment = candidateAssessment(
                CandidateAssessmentStatus.ASSIGNED, LocalDateTime.now().plusDays(1), application);
        when(candidateAssessmentRepository.findById(200L)).thenReturn(Optional.of(candidateAssessment));

        SubmitCandidateAssessmentRequest request = new SubmitCandidateAssessmentRequest();
        request.setSubmissionText("my answers");

        assertThatThrownBy(() -> assessmentService.submitCandidateAssessment(999L, 200L, request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void submitCandidateAssessment_flipsToNotSubmittedWhenDeadlinePassed() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        CandidateAssessment candidateAssessment = candidateAssessment(
                CandidateAssessmentStatus.ASSIGNED, LocalDateTime.now().minusDays(1), application);
        when(candidateAssessmentRepository.findById(200L)).thenReturn(Optional.of(candidateAssessment));
        when(candidateAssessmentRepository.save(any(CandidateAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubmitCandidateAssessmentRequest request = new SubmitCandidateAssessmentRequest();
        request.setSubmissionText("too late");

        assertThatThrownBy(() -> assessmentService.submitCandidateAssessment(1L, 200L, request))
                .isInstanceOf(ValidationException.class);
        assertThat(candidateAssessment.getStatus()).isEqualTo(CandidateAssessmentStatus.NOT_SUBMITTED);
    }

    @Test
    void submitCandidateAssessment_succeedsAndNotifiesCreator() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        CandidateAssessment candidateAssessment = candidateAssessment(
                CandidateAssessmentStatus.ASSIGNED, LocalDateTime.now().plusDays(1), application);
        when(candidateAssessmentRepository.findById(200L)).thenReturn(Optional.of(candidateAssessment));
        when(candidateAssessmentRepository.save(any(CandidateAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubmitCandidateAssessmentRequest request = new SubmitCandidateAssessmentRequest();
        request.setSubmissionText("my answers");

        CandidateAssessmentResponse response = assessmentService.submitCandidateAssessment(1L, 200L, request);

        assertThat(response.getStatus()).isEqualTo(CandidateAssessmentStatus.SUBMITTED);
        assertThat(response.getSubmissionText()).isEqualTo("my answers");
        verify(notificationService).notify(eq(2L), anyString(), anyString());
    }

    @Test
    void evaluateCandidateAssessment_throwsWhenNotSubmitted() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        CandidateAssessment candidateAssessment = candidateAssessment(
                CandidateAssessmentStatus.ASSIGNED, LocalDateTime.now().plusDays(1), application);
        when(candidateAssessmentRepository.findById(200L)).thenReturn(Optional.of(candidateAssessment));

        EvaluateCandidateAssessmentRequest request = new EvaluateCandidateAssessmentRequest();
        request.setScore(80);

        assertThatThrownBy(() -> assessmentService.evaluateCandidateAssessment(2L, 200L, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void evaluateCandidateAssessment_throwsWhenNotOwner() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        CandidateAssessment candidateAssessment = candidateAssessment(
                CandidateAssessmentStatus.SUBMITTED, LocalDateTime.now().plusDays(1), application);
        when(candidateAssessmentRepository.findById(200L)).thenReturn(Optional.of(candidateAssessment));

        EvaluateCandidateAssessmentRequest request = new EvaluateCandidateAssessmentRequest();
        request.setScore(80);

        assertThatThrownBy(() -> assessmentService.evaluateCandidateAssessment(999L, 200L, request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void evaluateCandidateAssessment_setsScoreAndNotifiesCandidate() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        CandidateAssessment candidateAssessment = candidateAssessment(
                CandidateAssessmentStatus.SUBMITTED, LocalDateTime.now().plusDays(1), application);
        when(candidateAssessmentRepository.findById(200L)).thenReturn(Optional.of(candidateAssessment));
        when(candidateAssessmentRepository.save(any(CandidateAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvaluateCandidateAssessmentRequest request = new EvaluateCandidateAssessmentRequest();
        request.setScore(88);
        request.setFeedback("Great work");

        CandidateAssessmentResponse response = assessmentService.evaluateCandidateAssessment(2L, 200L, request);

        assertThat(response.getStatus()).isEqualTo(CandidateAssessmentStatus.EVALUATED);
        assertThat(response.getScore()).isEqualTo(88);
        verify(notificationService).notify(eq(1L), anyString(), anyString());
    }

    @Test
    void compareForVacancy_throwsWhenNotOwner() {
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(vacancy()));

        assertThatThrownBy(() -> assessmentService.compareForVacancy(999L, 5L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void compareForVacancy_returnsResultsForOwner() {
        Application application = application(ApplicationStatus.ASSESSED);
        CandidateAssessment evaluated = candidateAssessment(
                CandidateAssessmentStatus.EVALUATED, LocalDateTime.now().plusDays(1), application);
        evaluated.setScore(90);
        when(jobVacancyRepository.findById(5L)).thenReturn(Optional.of(vacancy()));
        when(candidateAssessmentRepository.findByApplication_Vacancy_IdOrderByScoreDesc(5L)).thenReturn(List.of(evaluated));

        List<CandidateAssessmentResponse> responses = assessmentService.compareForVacancy(2L, 5L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getScore()).isEqualTo(90);
    }

    @Test
    void getCandidateAssessmentsForApplication_throwsWhenNotOwner() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application(ApplicationStatus.SHORTLISTED)));

        assertThatThrownBy(() -> assessmentService.getCandidateAssessmentsForApplication(999L, 100L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getMyAssessments_returnsCreatorsAssessmentsOnly() {
        when(assessmentRepository.findByCreatedByIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(assessment(false)));

        List<AssessmentResponse> responses = assessmentService.getMyAssessments(2L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Aptitude Round 1");
    }

    @Test
    void createAssessment_throwsWhenUserMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        CreateAssessmentRequest request = new CreateAssessmentRequest();
        request.setTitle("Technical Round");
        request.setDescription("Coding test");
        request.setType(AssessmentType.TECHNICAL);

        assertThatThrownBy(() -> assessmentService.createAssessment(2L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitCandidateAssessment_throwsWhenAlreadyEvaluated() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        CandidateAssessment candidateAssessment = candidateAssessment(
                CandidateAssessmentStatus.EVALUATED, LocalDateTime.now().plusDays(1), application);
        when(candidateAssessmentRepository.findById(200L)).thenReturn(Optional.of(candidateAssessment));

        SubmitCandidateAssessmentRequest request = new SubmitCandidateAssessmentRequest();
        request.setSubmissionText("too late to resubmit");

        assertThatThrownBy(() -> assessmentService.submitCandidateAssessment(1L, 200L, request))
                .isInstanceOf(ValidationException.class);
        verify(candidateAssessmentRepository, never()).save(any());
    }
}
