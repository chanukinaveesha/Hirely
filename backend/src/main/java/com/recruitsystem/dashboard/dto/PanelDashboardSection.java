package com.recruitsystem.dashboard.dto;

import com.recruitsystem.dto.interview.InterviewResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PanelDashboardSection implements DashboardSection {

    private List<InterviewResponse> upcomingInterviews;

    @Override
    public void applyTo(DashboardResponse.DashboardResponseBuilder builder) {
        builder.panel(this);
    }
}
