package com.recruitsystem.dto.assessment;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitCandidateAssessmentRequest {

    @NotBlank
    private String submissionText;
}
