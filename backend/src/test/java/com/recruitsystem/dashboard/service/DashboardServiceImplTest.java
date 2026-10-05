package com.recruitsystem.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recruitsystem.dashboard.dto.DashboardResponse;
import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.dashboard.strategy.DashboardStrategy;
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

    @Test
    void getDashboard_selectsStrategyMatchingRoleAndIgnoresOthers() {
        when(postService.getRecent(anyInt())).thenReturn(List.of(PostResponse.builder().id(1L).build()));
        DashboardSection candidateSection = mock(DashboardSection.class);
        DashboardStrategy candidateStrategy = mock(DashboardStrategy.class);
        when(candidateStrategy.getRole()).thenReturn(UserRole.JOB_SEEKER);
        when(candidateStrategy.buildSection(1L)).thenReturn(candidateSection);

        DashboardStrategy recruiterStrategy = mock(DashboardStrategy.class);
        when(recruiterStrategy.getRole()).thenReturn(UserRole.RECRUITER);

        DashboardServiceImpl service = new DashboardServiceImpl(postService, List.of(candidateStrategy, recruiterStrategy));

        service.getDashboard(1L, UserRole.JOB_SEEKER);

        verify(candidateSection).applyTo(any());
        verify(recruiterStrategy, never()).buildSection(anyLong());
    }

    @Test
    void getDashboard_alwaysIncludesRecentPosts() {
        when(postService.getRecent(anyInt())).thenReturn(List.of(PostResponse.builder().id(1L).build()));
        DashboardSection section = mock(DashboardSection.class);
        DashboardStrategy strategy = mock(DashboardStrategy.class);
        when(strategy.getRole()).thenReturn(UserRole.SYSTEM_ADMINISTRATOR);
        when(strategy.buildSection(5L)).thenReturn(section);

        DashboardServiceImpl service = new DashboardServiceImpl(postService, List.of(strategy));

        DashboardResponse response = service.getDashboard(5L, UserRole.SYSTEM_ADMINISTRATOR);

        assertThat(response.getRecentPosts()).hasSize(1);
    }

    @Test
    void getDashboard_unsupportedRole_throws() {
        DashboardServiceImpl service = new DashboardServiceImpl(postService, List.of());

        assertThatThrownBy(() -> service.getDashboard(1L, UserRole.JOB_SEEKER))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
