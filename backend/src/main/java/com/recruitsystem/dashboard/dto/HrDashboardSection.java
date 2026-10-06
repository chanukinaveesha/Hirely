package com.recruitsystem.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HrDashboardSection implements DashboardSection {

    private long assessmentsToEvaluate;
    private long interviewsToSchedule;

    @Override
    public void applyTo(DashboardResponse.DashboardResponseBuilder builder) {
        builder.hr(this);
    }
}
