package com.recruitsystem.dto.assessment;

import com.recruitsystem.entity.assessment.AssessmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAssessmentRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private AssessmentType type;
}
