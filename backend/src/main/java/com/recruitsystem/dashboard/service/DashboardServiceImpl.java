package com.recruitsystem.dashboard.service;

import com.recruitsystem.dashboard.dto.DashboardResponse;
import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.dashboard.strategy.DashboardContext;
import com.recruitsystem.dashboard.strategy.DashboardStrategy;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.post.service.PostService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Strategy Pattern - Client.
 * Selects the DashboardStrategy for the caller's role and runs it through a
 * fresh DashboardContext. No if-else/switch on role remains here: strategy
 * selection is a map lookup, and each DashboardSection applies itself to the
 * response builder.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_POSTS_LIMIT = 5;

    private final PostService postService;
    private final Map<UserRole, DashboardStrategy> strategiesByRole;

    public DashboardServiceImpl(PostService postService, List<DashboardStrategy> strategies) {
        this.postService = postService;
        this.strategiesByRole = strategies.stream()
                .collect(Collectors.toMap(DashboardStrategy::getRole, Function.identity()));
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId, UserRole role) {
        DashboardStrategy strategy = strategiesByRole.get(role);
        if (strategy == null) {
            throw new IllegalArgumentException("No dashboard strategy registered for role: " + role);
        }

        DashboardContext context = new DashboardContext();
        context.setStrategy(strategy);
        DashboardSection section = context.execute(userId);

        DashboardResponse.DashboardResponseBuilder response = DashboardResponse.builder()
                .recentPosts(postService.getRecent(RECENT_POSTS_LIMIT));
        section.applyTo(response);

        return response.build();
    }
}
