package com.recruitsystem.dto.interview;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class InterviewFeedbackResponse {

    private Long id;
    private Long interviewId;
    private Long panelMemberId;
    private String panelMemberName;
    private int score;
    private String comments;
    private LocalDateTime submittedAt;
}
