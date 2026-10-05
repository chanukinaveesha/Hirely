package com.recruitsystem.dashboard.dto;

import com.recruitsystem.entity.vacancy.VacancyStatus;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RecruiterDashboardSection implements DashboardSection {

    private Map<VacancyStatus, Long> vacancyCountsByStatus;
    private long applicantsAwaitingReview;
    private long shortlistedCount;

    @Override
    public void applyTo(DashboardResponse.DashboardResponseBuilder builder) {
        builder.recruiter(this);
    }
}
