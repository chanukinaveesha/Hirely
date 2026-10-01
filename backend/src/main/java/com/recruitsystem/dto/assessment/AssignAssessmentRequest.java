package com.recruitsystem.dto.assessment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignAssessmentRequest {

    @NotEmpty
    private List<Long> applicationIds;

    @NotNull
    @Future
    private LocalDateTime deadline;
}
