package com.recruitsystem.dashboard.dto;

import com.recruitsystem.entity.vacancy.VacancyStatus;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RecruiterDashboardSection {

    private Map<VacancyStatus, Long> vacancyCountsByStatus;
    private long applicantsAwaitingReview;
    private long shortlistedCount;
}
