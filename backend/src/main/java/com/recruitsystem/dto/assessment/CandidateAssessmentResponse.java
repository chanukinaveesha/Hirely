package com.recruitsystem.dto.assessment;

import com.recruitsystem.entity.assessment.AssessmentType;
import com.recruitsystem.entity.assessment.CandidateAssessmentStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CandidateAssessmentResponse {

    private Long id;
    private Long assessmentId;
    private String assessmentTitle;
    private String assessmentDescription;
    private AssessmentType assessmentType;
    private Long applicationId;
    private Long vacancyId;
    private String vacancyTitle;
    private Long candidateId;
    private String candidateName;
    private CandidateAssessmentStatus status;
    private LocalDateTime deadline;
    private String submissionText;
    private LocalDateTime submittedAt;
    private Integer score;
    private String feedback;
    private LocalDateTime evaluatedAt;
    private LocalDateTime assignedAt;
}
