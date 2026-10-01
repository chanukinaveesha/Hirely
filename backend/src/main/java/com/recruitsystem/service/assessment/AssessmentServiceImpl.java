package com.recruitsystem.service.assessment;

import com.recruitsystem.dto.assessment.AssessmentResponse;
import com.recruitsystem.dto.assessment.AssignAssessmentRequest;
import com.recruitsystem.dto.assessment.CandidateAssessmentResponse;
import com.recruitsystem.dto.assessment.CreateAssessmentRequest;
import com.recruitsystem.dto.assessment.EvaluateCandidateAssessmentRequest;
import com.recruitsystem.dto.assessment.SubmitCandidateAssessmentRequest;
import com.recruitsystem.entity.application.Application;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.assessment.Assessment;
import com.recruitsystem.entity.assessment.CandidateAssessment;
import com.recruitsystem.entity.assessment.CandidateAssessmentStatus;
import com.recruitsystem.entity.auth.User;
import com.recruitsystem.entity.vacancy.JobVacancy;
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
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private static final Set<ApplicationStatus> ASSIGNABLE_FROM =
            EnumSet.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEWING, ApplicationStatus.ASSESSED);

    private final AssessmentRepository assessmentRepository;
    private final CandidateAssessmentRepository candidateAssessmentRepository;
    private final ApplicationRepository applicationRepository;
    private final JobVacancyRepository jobVacancyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public AssessmentResponse createAssessment(Long hrId, CreateAssessmentRequest request) {
        User creator = userRepository.findById(hrId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + hrId));

        Assessment assessment = Assessment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .createdBy(creator)
                .archived(false)
                .build();

        return toResponse(assessmentRepository.save(assessment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentResponse> getMyAssessments(Long hrId) {
        return assessmentRepository.findByCreatedByIdOrderByCreatedAtDesc(hrId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AssessmentResponse archiveAssessment(Long hrId, Long assessmentId) {
        Assessment assessment = findOwnedAssessment(hrId, assessmentId);
        if (assessment.isArchived()) {
            throw new ValidationException("Assessment is already archived");
        }

        assessment.setArchived(true);
        return toResponse(assessmentRepository.save(assessment));
    }

    @Override
    @Transactional
    public List<CandidateAssessmentResponse> assignAssessment(Long hrId, Long assessmentId, AssignAssessmentRequest request) {
        Assessment assessment = findOwnedAssessment(hrId, assessmentId);
        if (assessment.isArchived()) {
            throw new ValidationException("Cannot assign an archived assessment");
        }

        List<CandidateAssessmentResponse> created = request.getApplicationIds().stream()
                .map(applicationId -> assignToApplication(hrId, assessment, applicationId, request.getDeadline()))
                .toList();

        return created;
    }

    private CandidateAssessmentResponse assignToApplication(Long hrId, Assessment assessment, Long applicationId, LocalDateTime deadline) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (!application.getVacancy().getPostedBy().getId().equals(hrId)) {
            throw new UnauthorizedException("You do not own this vacancy");
        }
        if (!ASSIGNABLE_FROM.contains(application.getStatus())) {
            throw new ValidationException("An assessment can only be assigned to a shortlisted or in-progress application");
        }
        if (candidateAssessmentRepository.existsByAssessmentIdAndApplicationId(assessment.getId(), applicationId)) {
            throw new DuplicateResourceException("This assessment has already been assigned for this vacancy");
        }

        CandidateAssessment candidateAssessment = CandidateAssessment.builder()
                .assessment(assessment)
                .application(application)
                .status(CandidateAssessmentStatus.ASSIGNED)
                .deadline(deadline)
                .build();
        CandidateAssessment saved = candidateAssessmentRepository.save(candidateAssessment);

        notificationService.notify(application.getJobSeeker().getId(), "ASSESSMENT_ASSIGNED",
                "You have been assigned the '" + assessment.getTitle() + "' assessment for '"
                        + application.getVacancy().getTitle() + "'. Submit by " + deadline + ".");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<CandidateAssessmentResponse> getMyCandidateAssessments(Long jobSeekerId) {
        return candidateAssessmentRepository.findByApplication_JobSeeker_IdOrderByDeadlineAsc(jobSeekerId).stream()
                .map(this::refreshDeadlineStatus)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CandidateAssessmentResponse submitCandidateAssessment(
            Long jobSeekerId, Long candidateAssessmentId, SubmitCandidateAssessmentRequest request) {
        CandidateAssessment candidateAssessment = candidateAssessmentRepository.findById(candidateAssessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment assignment not found: " + candidateAssessmentId));
        if (!candidateAssessment.getApplication().getJobSeeker().getId().equals(jobSeekerId)) {
            throw new UnauthorizedException("This assessment does not belong to you");
        }

        candidateAssessment = refreshDeadlineStatus(candidateAssessment);
        if (candidateAssessment.getStatus() != CandidateAssessmentStatus.ASSIGNED) {
            throw new ValidationException("This assessment can no longer be submitted");
        }

        candidateAssessment.setSubmissionText(request.getSubmissionText());
        candidateAssessment.setSubmittedAt(LocalDateTime.now());
        candidateAssessment.setStatus(CandidateAssessmentStatus.SUBMITTED);
        CandidateAssessment saved = candidateAssessmentRepository.save(candidateAssessment);

        Application application = saved.getApplication();
        notificationService.notify(saved.getAssessment().getCreatedBy().getId(), "ASSESSMENT_SUBMITTED",
                application.getJobSeeker().getName() + " submitted the '" + saved.getAssessment().getTitle()
                        + "' assessment for '" + application.getVacancy().getTitle() + "'.");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public CandidateAssessmentResponse evaluateCandidateAssessment(
            Long hrId, Long candidateAssessmentId, EvaluateCandidateAssessmentRequest request) {
        CandidateAssessment candidateAssessment = findOwnedCandidateAssessment(hrId, candidateAssessmentId);

        candidateAssessment = refreshDeadlineStatus(candidateAssessment);
        if (candidateAssessment.getStatus() != CandidateAssessmentStatus.SUBMITTED) {
            throw new ValidationException("Only a submitted assessment can be evaluated");
        }

        candidateAssessment.setScore(request.getScore());
        candidateAssessment.setFeedback(request.getFeedback());
        candidateAssessment.setEvaluatedAt(LocalDateTime.now());
        candidateAssessment.setStatus(CandidateAssessmentStatus.EVALUATED);
        CandidateAssessment saved = candidateAssessmentRepository.save(candidateAssessment);

        Application application = saved.getApplication();
        notificationService.notify(application.getJobSeeker().getId(), "ASSESSMENT_EVALUATED",
                "Your '" + saved.getAssessment().getTitle() + "' assessment for '"
                        + application.getVacancy().getTitle() + "' has been evaluated. Score: " + saved.getScore() + ".");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<CandidateAssessmentResponse> getCandidateAssessmentsForApplication(Long hrId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (!application.getVacancy().getPostedBy().getId().equals(hrId)) {
            throw new UnauthorizedException("You do not own this vacancy");
        }

        return candidateAssessmentRepository.findByApplicationIdOrderByAssignedAtDesc(applicationId).stream()
                .map(this::refreshDeadlineStatus)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<CandidateAssessmentResponse> compareForVacancy(Long hrId, Long vacancyId) {
        JobVacancy vacancy = jobVacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: " + vacancyId));
        if (!vacancy.getPostedBy().getId().equals(hrId)) {
            throw new UnauthorizedException("You do not own this vacancy");
        }

        return candidateAssessmentRepository.findByApplication_Vacancy_IdOrderByScoreDesc(vacancyId).stream()
                .map(this::refreshDeadlineStatus)
                .map(this::toResponse)
                .toList();
    }

    private Assessment findOwnedAssessment(Long hrId, Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));
        if (!assessment.getCreatedBy().getId().equals(hrId)) {
            throw new UnauthorizedException("You do not own this assessment");
        }
        return assessment;
    }

    private CandidateAssessment findOwnedCandidateAssessment(Long hrId, Long candidateAssessmentId) {
        CandidateAssessment candidateAssessment = candidateAssessmentRepository.findById(candidateAssessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment assignment not found: " + candidateAssessmentId));
        if (!candidateAssessment.getApplication().getVacancy().getPostedBy().getId().equals(hrId)) {
            throw new UnauthorizedException("You do not own this assessment assignment");
        }
        return candidateAssessment;
    }

    private CandidateAssessment refreshDeadlineStatus(CandidateAssessment candidateAssessment) {
        if (candidateAssessment.getStatus() == CandidateAssessmentStatus.ASSIGNED
                && LocalDateTime.now().isAfter(candidateAssessment.getDeadline())) {
            candidateAssessment.setStatus(CandidateAssessmentStatus.NOT_SUBMITTED);
            return candidateAssessmentRepository.save(candidateAssessment);
        }
        return candidateAssessment;
    }

    private AssessmentResponse toResponse(Assessment assessment) {
        return AssessmentResponse.builder()
                .id(assessment.getId())
                .title(assessment.getTitle())
                .description(assessment.getDescription())
                .type(assessment.getType())
                .archived(assessment.isArchived())
                .createdByName(assessment.getCreatedBy().getName())
                .createdAt(assessment.getCreatedAt())
                .updatedAt(assessment.getUpdatedAt())
                .build();
    }

    private CandidateAssessmentResponse toResponse(CandidateAssessment candidateAssessment) {
        Assessment assessment = candidateAssessment.getAssessment();
        Application application = candidateAssessment.getApplication();

        return CandidateAssessmentResponse.builder()
                .id(candidateAssessment.getId())
                .assessmentId(assessment.getId())
                .assessmentTitle(assessment.getTitle())
                .assessmentDescription(assessment.getDescription())
                .assessmentType(assessment.getType())
                .applicationId(application.getId())
                .vacancyId(application.getVacancy().getId())
                .vacancyTitle(application.getVacancy().getTitle())
                .candidateId(application.getJobSeeker().getId())
                .candidateName(application.getJobSeeker().getName())
                .status(candidateAssessment.getStatus())
                .deadline(candidateAssessment.getDeadline())
                .submissionText(candidateAssessment.getSubmissionText())
                .submittedAt(candidateAssessment.getSubmittedAt())
                .score(candidateAssessment.getScore())
                .feedback(candidateAssessment.getFeedback())
                .evaluatedAt(candidateAssessment.getEvaluatedAt())
                .assignedAt(candidateAssessment.getAssignedAt())
                .build();
    }
}
