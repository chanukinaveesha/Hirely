package com.recruitsystem.dashboard.dto;

import com.recruitsystem.dto.interview.InterviewResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PanelDashboardSection {

    private List<InterviewResponse> upcomingInterviews;
}
