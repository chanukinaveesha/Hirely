package com.recruitsystem.dto.interview;

import com.recruitsystem.entity.interview.InterviewStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class InterviewResponse {

    private Long id;
    private Long applicationId;
    private String vacancyTitle;
    private String candidateName;
    private LocalDateTime scheduledAt;
    private String location;
    private InterviewStatus status;
    private LocalDateTime candidatePreferredAt;
    private List<PanelMemberSummaryResponse> panelMembers;
    private int panelMemberCount;
    private int feedbackSubmittedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
