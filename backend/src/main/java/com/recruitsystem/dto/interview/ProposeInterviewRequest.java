package com.recruitsystem.dto.interview;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProposeInterviewRequest {

    @NotNull
    private Long applicationId;

    @NotNull
    @Future
    private LocalDateTime scheduledAt;

    private String location;

    @NotEmpty
    private List<Long> panelMemberIds;
}
