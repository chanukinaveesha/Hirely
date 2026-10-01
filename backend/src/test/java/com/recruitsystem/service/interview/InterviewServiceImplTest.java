package com.recruitsystem.service.interview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recruitsystem.dto.interview.CandidateRescheduleRequest;
import com.recruitsystem.dto.interview.InterviewFeedbackResponse;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.dto.interview.ProposeInterviewRequest;
import com.recruitsystem.dto.interview.RescheduleInterviewRequest;
import com.recruitsystem.dto.interview.SubmitFeedbackRequest;
import com.recruitsystem.entity.application.Application;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.InterviewPanelMember;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.company.ClientCompany;
import com.recruitsystem.entity.interview.Interview;
import com.recruitsystem.entity.interview.InterviewFeedback;
import com.recruitsystem.entity.interview.InterviewStatus;
import com.recruitsystem.entity.vacancy.JobVacancy;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.application.ApplicationRepository;
import com.recruitsystem.repository.auth.InterviewPanelMemberRepository;
import com.recruitsystem.repository.interview.InterviewFeedbackRepository;
import com.recruitsystem.repository.interview.InterviewRepository;
import com.recruitsystem.service.application.ApplicationService;
import com.recruitsystem.service.notification.NotificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewServiceImplTest {

    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private InterviewFeedbackRepository interviewFeedbackRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private InterviewPanelMemberRepository interviewPanelMemberRepository;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private NotificationService notificationService;

    private InterviewServiceImpl interviewService;

    @BeforeEach
    void setUp() {
        interviewService = new InterviewServiceImpl(
                interviewRepository, interviewFeedbackRepository, applicationRepository,
                interviewPanelMemberRepository, applicationService, notificationService);
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

    private Recruiter recruiter() {
        ClientCompany company = ClientCompany.builder().id(10L).companyName("Acme Corp").build();
        return Recruiter.builder().id(2L).name("Rita").email("rita@example.com")
                .passwordHash("hashed").accountStatus(AccountStatus.ACTIVE).clientCompany(company).build();
    }

    private JobVacancy vacancy() {
        return JobVacancy.builder()
                .id(5L)
                .title("Backend Engineer")
                .category(VacancyCategory.IT)
                .status(VacancyStatus.PUBLISHED)
                .deadline(LocalDate.now().plusDays(10))
                .postedBy(recruiter())
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

    private InterviewPanelMember panelMember(Long id) {
        return InterviewPanelMember.builder()
                .id(id)
                .name("Panelist " + id)
                .email("panel" + id + "@example.com")
                .passwordHash("hashed")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    private Interview interview(InterviewStatus status, Application application, Set<InterviewPanelMember> panelMembers) {
        return Interview.builder()
                .id(200L)
                .application(application)
                .scheduledAt(LocalDateTime.now().plusDays(3))
                .location("Room 1")
                .status(status)
                .panelMembers(panelMembers)
                .build();
    }

    private ProposeInterviewRequest proposeRequest(Long applicationId, List<Long> panelMemberIds) {
        ProposeInterviewRequest request = new ProposeInterviewRequest();
        request.setApplicationId(applicationId);
        request.setScheduledAt(LocalDateTime.now().plusDays(3));
        request.setLocation("Room 1");
        request.setPanelMemberIds(panelMemberIds);
        return request;
    }

    @Test
    void propose_throwsWhenCallerDoesNotOwnVacancy() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> interviewService.propose(999L, proposeRequest(100L, List.of(3L))))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void propose_throwsWhenApplicationNotShortlistedOrInterviewing() {
        Application application = application(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> interviewService.propose(2L, proposeRequest(100L, List.of(3L))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void propose_throwsWhenPanelMemberMissing() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(interviewPanelMemberRepository.findAllById(List.of(3L))).thenReturn(List.of());

        assertThatThrownBy(() -> interviewService.propose(2L, proposeRequest(100L, List.of(3L))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void propose_createsInterviewAndNotifiesCandidateAndPanel() {
        Application application = application(ApplicationStatus.SHORTLISTED);
        InterviewPanelMember panelMember = panelMember(3L);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(interviewPanelMemberRepository.findAllById(List.of(3L))).thenReturn(List.of(panelMember));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interviewFeedbackRepository.countByInterviewId(any())).thenReturn(0L);

        InterviewResponse response = interviewService.propose(2L, proposeRequest(100L, List.of(3L)));

        assertThat(response.getStatus()).isEqualTo(InterviewStatus.PROPOSED);
        assertThat(response.getPanelMemberCount()).isEqualTo(1);
        verify(applicationService).markInterviewing(100L);
        verify(notificationService).notify(eq(1L), anyString(), anyString());
        verify(notificationService).notify(eq(3L), anyString(), anyString());
    }

    @Test
    void reschedule_throwsWhenCancelled() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CANCELLED, application, new HashSet<>());
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));

        RescheduleInterviewRequest request = new RescheduleInterviewRequest();
        request.setScheduledAt(LocalDateTime.now().plusDays(5));

        assertThatThrownBy(() -> interviewService.reschedule(2L, 200L, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void reschedule_resetsToProposedAndClearsCandidatePreference() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.RESCHEDULE_REQUESTED, application, new HashSet<>());
        interview.setCandidatePreferredAt(LocalDateTime.now().plusDays(1));
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interviewFeedbackRepository.countByInterviewId(any())).thenReturn(0L);

        RescheduleInterviewRequest request = new RescheduleInterviewRequest();
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);
        request.setScheduledAt(newTime);

        InterviewResponse response = interviewService.reschedule(2L, 200L, request);

        assertThat(response.getStatus()).isEqualTo(InterviewStatus.PROPOSED);
        assertThat(response.getScheduledAt()).isEqualTo(newTime);
        assertThat(response.getCandidatePreferredAt()).isNull();
    }

    @Test
    void cancel_throwsWhenAlreadyCancelled() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CANCELLED, application, new HashSet<>());
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));

        assertThatThrownBy(() -> interviewService.cancel(2L, 200L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void cancel_setsStatusAndNotifiesCandidate() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CONFIRMED, application, new HashSet<>());
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interviewFeedbackRepository.countByInterviewId(any())).thenReturn(0L);

        InterviewResponse response = interviewService.cancel(2L, 200L);

        assertThat(response.getStatus()).isEqualTo(InterviewStatus.CANCELLED);
        verify(notificationService).notify(eq(1L), anyString(), anyString());
    }

    @Test
    void accept_throwsWhenInterviewNotOwnedByCandidate() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.PROPOSED, application, new HashSet<>());
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));

        assertThatThrownBy(() -> interviewService.accept(999L, 200L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void accept_throwsWhenNotInAcceptableState() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CONFIRMED, application, new HashSet<>());
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));

        assertThatThrownBy(() -> interviewService.accept(1L, 200L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void accept_confirmsAndNotifiesRecruiter() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.PROPOSED, application, new HashSet<>());
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interviewFeedbackRepository.countByInterviewId(any())).thenReturn(0L);

        InterviewResponse response = interviewService.accept(1L, 200L);

        assertThat(response.getStatus()).isEqualTo(InterviewStatus.CONFIRMED);
        verify(notificationService).notify(eq(2L), anyString(), anyString());
    }

    @Test
    void requestReschedule_setsCandidatePreferenceAndNotifiesRecruiter() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.PROPOSED, application, new HashSet<>());
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interviewFeedbackRepository.countByInterviewId(any())).thenReturn(0L);

        CandidateRescheduleRequest request = new CandidateRescheduleRequest();
        LocalDateTime preferred = LocalDateTime.now().plusDays(7);
        request.setPreferredAt(preferred);
        request.setReason("Conflict");

        InterviewResponse response = interviewService.requestReschedule(1L, 200L, request);

        assertThat(response.getStatus()).isEqualTo(InterviewStatus.RESCHEDULE_REQUESTED);
        assertThat(response.getCandidatePreferredAt()).isEqualTo(preferred);
        verify(notificationService).notify(eq(2L), anyString(), anyString());
    }

    @Test
    void submitFeedback_throwsWhenPanelMemberNotAssigned() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CONFIRMED, application, new HashSet<>());
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));

        SubmitFeedbackRequest request = new SubmitFeedbackRequest();
        request.setScore(8);

        assertThatThrownBy(() -> interviewService.submitFeedback(3L, 200L, request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void submitFeedback_upsertsAndTriggersAssessedWhenAllPanelMembersSubmitted() {
        InterviewPanelMember panelMember = panelMember(3L);
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CONFIRMED, application, new HashSet<>(Set.of(panelMember)));
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));
        when(interviewFeedbackRepository.findByInterviewIdAndPanelMemberId(200L, 3L)).thenReturn(Optional.empty());
        when(interviewPanelMemberRepository.findById(3L)).thenReturn(Optional.of(panelMember));
        when(interviewFeedbackRepository.save(any(InterviewFeedback.class))).thenAnswer(invocation -> {
            InterviewFeedback feedback = invocation.getArgument(0);
            feedback.setId(300L);
            return feedback;
        });
        when(interviewFeedbackRepository.countByInterviewId(200L)).thenReturn(1L);

        SubmitFeedbackRequest request = new SubmitFeedbackRequest();
        request.setScore(9);
        request.setComments("Strong candidate");

        InterviewFeedbackResponse response = interviewService.submitFeedback(3L, 200L, request);

        assertThat(response.getScore()).isEqualTo(9);
        verify(applicationService).markAssessed(100L);
    }

    @Test
    void submitFeedback_doesNotTriggerAssessedWhenPanelStillPending() {
        InterviewPanelMember panelMember1 = panelMember(3L);
        InterviewPanelMember panelMember2 = panelMember(4L);
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CONFIRMED, application, new HashSet<>(Set.of(panelMember1, panelMember2)));
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));
        when(interviewFeedbackRepository.findByInterviewIdAndPanelMemberId(200L, 3L)).thenReturn(Optional.empty());
        when(interviewPanelMemberRepository.findById(3L)).thenReturn(Optional.of(panelMember1));
        when(interviewFeedbackRepository.save(any(InterviewFeedback.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interviewFeedbackRepository.countByInterviewId(200L)).thenReturn(1L);

        SubmitFeedbackRequest request = new SubmitFeedbackRequest();
        request.setScore(7);

        interviewService.submitFeedback(3L, 200L, request);

        verify(applicationService, never()).markAssessed(any());
    }

    @Test
    void submitFeedback_updatesExistingFeedbackInsteadOfDuplicating() {
        InterviewPanelMember panelMember = panelMember(3L);
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CONFIRMED, application, new HashSet<>(Set.of(panelMember)));
        InterviewFeedback existing = InterviewFeedback.builder()
                .id(300L).interview(interview).panelMember(panelMember).score(5).build();
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));
        when(interviewFeedbackRepository.findByInterviewIdAndPanelMemberId(200L, 3L)).thenReturn(Optional.of(existing));
        when(interviewFeedbackRepository.save(any(InterviewFeedback.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interviewFeedbackRepository.countByInterviewId(200L)).thenReturn(1L);

        SubmitFeedbackRequest request = new SubmitFeedbackRequest();
        request.setScore(10);
        request.setComments("Updated");

        InterviewFeedbackResponse response = interviewService.submitFeedback(3L, 200L, request);

        assertThat(response.getScore()).isEqualTo(10);
        assertThat(response.getId()).isEqualTo(300L);
        verify(interviewPanelMemberRepository, never()).findById(any());
    }

    @Test
    void getFeedbackForInterview_throwsWhenCallerDoesNotOwnInterview() {
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CONFIRMED, application, new HashSet<>());
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));

        assertThatThrownBy(() -> interviewService.getFeedbackForInterview(999L, 200L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getFeedbackForInterview_returnsFeedbackList() {
        InterviewPanelMember panelMember = panelMember(3L);
        Application application = application(ApplicationStatus.INTERVIEWING);
        Interview interview = interview(InterviewStatus.CONFIRMED, application, new HashSet<>(Set.of(panelMember)));
        InterviewFeedback feedback = InterviewFeedback.builder()
                .id(300L).interview(interview).panelMember(panelMember).score(8).build();
        when(interviewRepository.findById(200L)).thenReturn(Optional.of(interview));
        when(interviewFeedbackRepository.findByInterviewId(200L)).thenReturn(List.of(feedback));

        List<InterviewFeedbackResponse> responses = interviewService.getFeedbackForInterview(2L, 200L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getScore()).isEqualTo(8);
    }
}
