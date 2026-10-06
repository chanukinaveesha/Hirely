package com.recruitsystem.dashboard.strategy;

import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.dashboard.dto.PanelDashboardSection;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.service.interview.InterviewService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Strategy Pattern - Concrete Strategy for the INTERVIEW_PANEL_MEMBER role.
 */
@Component
@RequiredArgsConstructor
public class PanelDashboardStrategy implements DashboardStrategy {

    private static final int RECENT_LIST_LIMIT = 5;

    private final InterviewService interviewService;

    @Override
    public UserRole getRole() {
        return UserRole.INTERVIEW_PANEL_MEMBER;
    }

    @Override
    public DashboardSection buildSection(Long panelMemberId) {
        List<InterviewResponse> upcomingInterviews = interviewService.getMyPanelInterviews(panelMemberId).stream()
                .filter(i -> i.getScheduledAt() != null && i.getScheduledAt().isAfter(LocalDateTime.now()))
                .limit(RECENT_LIST_LIMIT)
                .toList();

        return PanelDashboardSection.builder().upcomingInterviews(upcomingInterviews).build();
    }
}
