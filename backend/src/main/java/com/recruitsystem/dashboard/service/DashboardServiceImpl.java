package com.recruitsystem.dashboard.service;

import com.recruitsystem.dashboard.dto.DashboardResponse;
import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.dashboard.strategy.DashboardContext;
import com.recruitsystem.dashboard.strategy.DashboardStrategy;
import com.recruitsystem.dashboard.strategy.DashboardStrategyFactory;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Strategy Pattern - Client.
 * Factory Pattern - Client.
 * Gets the DashboardStrategy for the caller's role from DashboardStrategyFactory
 * and runs it through a fresh DashboardContext. No if-else/switch on role and
 * no concrete strategy class remains here: selection is fully hidden behind
 * the factory, and each DashboardSection applies itself to the response builder.
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_POSTS_LIMIT = 5;

    private final PostService postService;
    private final DashboardStrategyFactory dashboardStrategyFactory;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId, UserRole role) {
        DashboardStrategy strategy = dashboardStrategyFactory.getStrategy(role);

        DashboardContext context = new DashboardContext();
        context.setStrategy(strategy);
        DashboardSection section = context.execute(userId);

        DashboardResponse.DashboardResponseBuilder response = DashboardResponse.builder()
                .recentPosts(postService.getRecent(RECENT_POSTS_LIMIT));
        section.applyTo(response);

        return response.build();
    }
}
