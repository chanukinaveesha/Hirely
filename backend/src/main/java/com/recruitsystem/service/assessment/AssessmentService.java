package com.recruitsystem.service.assessment;

import com.recruitsystem.dto.assessment.AssessmentResponse;
import com.recruitsystem.dto.assessment.AssignAssessmentRequest;
import com.recruitsystem.dto.assessment.CandidateAssessmentResponse;
import com.recruitsystem.dto.assessment.CreateAssessmentRequest;
import com.recruitsystem.dto.assessment.EvaluateCandidateAssessmentRequest;
import com.recruitsystem.dto.assessment.SubmitCandidateAssessmentRequest;
import java.util.List;

public interface AssessmentService {

    AssessmentResponse createAssessment(Long hrId, CreateAssessmentRequest request);

    List<AssessmentResponse> getMyAssessments(Long hrId);

    AssessmentResponse archiveAssessment(Long hrId, Long assessmentId);

    List<CandidateAssessmentResponse> assignAssessment(Long hrId, Long assessmentId, AssignAssessmentRequest request);

    List<CandidateAssessmentResponse> getMyCandidateAssessments(Long jobSeekerId);

    CandidateAssessmentResponse submitCandidateAssessment(
            Long jobSeekerId, Long candidateAssessmentId, SubmitCandidateAssessmentRequest request);

    CandidateAssessmentResponse evaluateCandidateAssessment(
            Long hrId, Long candidateAssessmentId, EvaluateCandidateAssessmentRequest request);

    List<CandidateAssessmentResponse> getCandidateAssessmentsForApplication(Long hrId, Long applicationId);

    List<CandidateAssessmentResponse> compareForVacancy(Long hrId, Long vacancyId);
}
