package com.recruitsystem.service.application;

import com.recruitsystem.dto.application.ApplicationResponse;
import com.recruitsystem.dto.application.ApplyToVacancyRequest;
import java.util.List;

public interface ApplicationService {

    ApplicationResponse apply(Long jobSeekerId, ApplyToVacancyRequest request);

    List<ApplicationResponse> getMyApplications(Long jobSeekerId);

    void withdraw(Long jobSeekerId, Long applicationId);

    List<ApplicationResponse> getApplicantsForVacancy(Long recruiterId, Long vacancyId);

    ApplicationResponse shortlist(Long recruiterId, Long applicationId);

    ApplicationResponse reject(Long recruiterId, Long applicationId);

    ApplicationResponse select(Long recruiterId, Long applicationId);

    // Internal status nudges — no controller endpoint of their own. Called by
    // the interview module as a side effect of scheduling/assessing, not
    // directly by a user action.
    void markInterviewing(Long applicationId);

    void markAssessed(Long applicationId);
}
