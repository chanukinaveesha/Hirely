package com.recruitsystem.dto.assessment;

import com.recruitsystem.entity.assessment.AssessmentType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AssessmentResponse {

    private Long id;
    private String title;
    private String description;
    private AssessmentType type;
    private boolean archived;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
