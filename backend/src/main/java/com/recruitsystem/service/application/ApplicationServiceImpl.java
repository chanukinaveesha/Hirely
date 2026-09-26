package com.recruitsystem.service.application;

import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.application.ApplyToVacancyRequest;
import com.recruitsystem.entity.application.Application;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.vacancy.JobVacancy;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    // Once an application lands in one of these it's done; withdrawing no longer makes sense.
    private static final Set<ApplicationStatus> TERMINAL_STATUSES =
            EnumSet.of(ApplicationStatus.WITHDRAWN, ApplicationStatus.REJECTED, ApplicationStatus.SELECTED);

    private static final Set<ApplicationStatus> SHORTLISTABLE_FROM =
            EnumSet.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW);

    private static final Set<ApplicationStatus> NOT_REJECTABLE_FROM =
            EnumSet.of(ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN, ApplicationStatus.SELECTED);

    private static final Set<ApplicationStatus> SELECTABLE_FROM = EnumSet.of(
            ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEWING, ApplicationStatus.ASSESSED);

    private final ApplicationRepository applicationRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final JobVacancyRepository jobVacancyRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ApplicationResponse apply(Long jobSeekerId, ApplyToVacancyRequest request) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + jobSeekerId));
        JobVacancy vacancy = jobVacancyRepository.findById(request.getVacancyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: " + request.getVacancyId()));

        if (vacancy.getStatus() != VacancyStatus.PUBLISHED) {
            throw new ValidationException("This vacancy is not open for applications");
        }
        if (vacancy.getDeadline().isBefore(LocalDate.now())) {
            throw new ValidationException("The application deadline for this vacancy has passed");
        }
        if (applicationRepository.existsByJobSeekerIdAndVacancyId(jobSeekerId, vacancy.getId())) {
            throw new DuplicateResourceException("You have already applied to this vacancy");
        }

        Application application = Application.builder()
                .jobSeeker(jobSeeker)
                .vacancy(vacancy)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        return toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(Long jobSeekerId) {
        return applicationRepository.findByJobSeekerIdOrderByAppliedAtDesc(jobSeekerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void withdraw(Long jobSeekerId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!application.getJobSeeker().getId().equals(jobSeekerId)) {
            throw new UnauthorizedException("You do not own this application");
        }
        if (TERMINAL_STATUSES.contains(application.getStatus())) {
            throw new ValidationException("This application can no longer be withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicantsForVacancy(Long recruiterId, Long vacancyId) {
        JobVacancy vacancy = jobVacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: " + vacancyId));
        if (!vacancy.getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You do not own this vacancy");
        }

        return applicationRepository.findByVacancyIdOrderByAppliedAtDesc(vacancyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ApplicationResponse shortlist(Long recruiterId, Long applicationId) {
        Application application = findOwnedApplication(recruiterId, applicationId);
        if (!SHORTLISTABLE_FROM.contains(application.getStatus())) {
            throw new ValidationException("Only submitted or under-review applications can be shortlisted");
        }

        application.setStatus(ApplicationStatus.SHORTLISTED);
        Application saved = applicationRepository.save(application);
        notifyCandidate(saved, "Your application for '" + saved.getVacancy().getTitle() + "' has been shortlisted.");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ApplicationResponse reject(Long recruiterId, Long applicationId) {
        Application application = findOwnedApplication(recruiterId, applicationId);
        if (NOT_REJECTABLE_FROM.contains(application.getStatus())) {
            throw new ValidationException("This application can no longer be rejected");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        Application saved = applicationRepository.save(application);
        notifyCandidate(saved, "Your application for '" + saved.getVacancy().getTitle() + "' was not successful.");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ApplicationResponse select(Long recruiterId, Long applicationId) {
        Application application = findOwnedApplication(recruiterId, applicationId);
        if (!SELECTABLE_FROM.contains(application.getStatus())) {
            throw new ValidationException("Only shortlisted, interviewing, or assessed applications can be selected");
        }

        application.setStatus(ApplicationStatus.SELECTED);
        Application saved = applicationRepository.save(application);
        notifyCandidate(saved, "Congratulations! You have been selected for '" + saved.getVacancy().getTitle() + "'.");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void markInterviewing(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (application.getStatus() == ApplicationStatus.SHORTLISTED) {
            application.setStatus(ApplicationStatus.INTERVIEWING);
            applicationRepository.save(application);
        }
    }

    @Override
    @Transactional
    public void markAssessed(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (application.getStatus() == ApplicationStatus.INTERVIEWING) {
            application.setStatus(ApplicationStatus.ASSESSED);
            applicationRepository.save(application);
        }
    }

    private Application findOwnedApplication(Long recruiterId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (!application.getVacancy().getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You do not own this vacancy");
        }
        return application;
    }

    private void notifyCandidate(Application application, String message) {
        notificationService.notify(application.getJobSeeker().getId(), "APPLICATION_STATUS_UPDATE", message);
    }

    private ApplicationResponse toResponse(Application application) {
        JobVacancy vacancy = application.getVacancy();
        JobSeeker jobSeeker = application.getJobSeeker();
        return ApplicationResponse.builder()
                .id(application.getId())
                .vacancyId(vacancy.getId())
                .vacancyTitle(vacancy.getTitle())
                .clientCompanyName(vacancy.getClientCompany().getCompanyName())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .jobSeekerId(jobSeeker.getId())
                .jobSeekerName(jobSeeker.getName())
                .jobSeekerEmail(jobSeeker.getEmail())
                .build();
    }
}
