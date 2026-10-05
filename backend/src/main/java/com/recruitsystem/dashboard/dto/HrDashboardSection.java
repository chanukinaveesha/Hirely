package com.recruitsystem.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HrDashboardSection {

    private long assessmentsToEvaluate;
    private long interviewsToSchedule;
}
