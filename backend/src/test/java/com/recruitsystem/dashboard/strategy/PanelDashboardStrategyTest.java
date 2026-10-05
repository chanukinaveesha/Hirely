package com.recruitsystem.dashboard.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.recruitsystem.dashboard.dto.PanelDashboardSection;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.service.interview.InterviewService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PanelDashboardStrategyTest {

    @Mock private InterviewService interviewService;

    private PanelDashboardStrategy strategy;

    @Test
    void getRole_returnsInterviewPanelMember() {
        strategy = new PanelDashboardStrategy(interviewService);
        assertThat(strategy.getRole()).isEqualTo(UserRole.INTERVIEW_PANEL_MEMBER);
    }

    @Test
    void buildSection_filtersToFutureInterviews() {
        strategy = new PanelDashboardStrategy(interviewService);
        when(interviewService.getMyPanelInterviews(4L)).thenReturn(List.of(
                InterviewResponse.builder().id(10L).scheduledAt(LocalDateTime.now().plusDays(1)).build(),
                InterviewResponse.builder().id(11L).scheduledAt(LocalDateTime.now().minusDays(1)).build()));

        PanelDashboardSection section = (PanelDashboardSection) strategy.buildSection(4L);

        assertThat(section.getUpcomingInterviews()).hasSize(1);
        assertThat(section.getUpcomingInterviews().get(0).getId()).isEqualTo(10L);
    }
}
