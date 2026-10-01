package com.recruitsystem.dto.interview;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandidateRescheduleRequest {

    @NotNull
    @Future
    private LocalDateTime preferredAt;

    private String reason;
}
