package com.recruitsystem.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recruitsystem.dashboard.dto.DashboardResponse;
import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.dashboard.strategy.DashboardStrategy;
import com.recruitsystem.dashboard.strategy.DashboardStrategyFactory;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.post.dto.PostResponse;
import com.recruitsystem.post.service.PostService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private PostService postService;
    @Mock private DashboardStrategyFactory dashboardStrategyFactory;

    @Test
    void getDashboard_runsTheStrategyReturnedByTheFactory() {
        when(postService.getRecent(anyInt())).thenReturn(List.of(PostResponse.builder().id(1L).build()));
        DashboardSection section = mock(DashboardSection.class);
        DashboardStrategy strategy = mock(DashboardStrategy.class);
        when(strategy.buildSection(1L)).thenReturn(section);
        when(dashboardStrategyFactory.getStrategy(UserRole.JOB_SEEKER)).thenReturn(strategy);

        DashboardServiceImpl service = new DashboardServiceImpl(postService, dashboardStrategyFactory);

        service.getDashboard(1L, UserRole.JOB_SEEKER);

        verify(section).applyTo(any());
    }

    @Test
    void getDashboard_alwaysIncludesRecentPosts() {
        when(postService.getRecent(anyInt())).thenReturn(List.of(PostResponse.builder().id(1L).build()));
        DashboardSection section = mock(DashboardSection.class);
        DashboardStrategy strategy = mock(DashboardStrategy.class);
        when(strategy.buildSection(5L)).thenReturn(section);
        when(dashboardStrategyFactory.getStrategy(UserRole.SYSTEM_ADMINISTRATOR)).thenReturn(strategy);

        DashboardServiceImpl service = new DashboardServiceImpl(postService, dashboardStrategyFactory);

        DashboardResponse response = service.getDashboard(5L, UserRole.SYSTEM_ADMINISTRATOR);

        assertThat(response.getRecentPosts()).hasSize(1);
    }

    @Test
    void getDashboard_propagatesFactoryExceptionForUnsupportedRole() {
        when(dashboardStrategyFactory.getStrategy(UserRole.JOB_SEEKER))
                .thenThrow(new IllegalArgumentException("No dashboard strategy registered for role: JOB_SEEKER"));

        DashboardServiceImpl service = new DashboardServiceImpl(postService, dashboardStrategyFactory);

        assertThatThrownBy(() -> service.getDashboard(1L, UserRole.JOB_SEEKER))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
