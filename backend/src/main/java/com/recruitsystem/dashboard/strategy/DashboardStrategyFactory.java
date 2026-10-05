package com.recruitsystem.dashboard.strategy;

import com.recruitsystem.entity.auth.UserRole;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern - Factory.
 * Hides concrete strategy selection behind getStrategy(role): the client
 * (DashboardServiceImpl) depends only on this factory and the DashboardStrategy
 * interface, never on a concrete strategy class.
 * Adaptation note: the lecture's Vehicle factory builds its own products; here
 * the strategies need Spring-injected repositories/services, so Spring builds
 * the concrete strategy beans and the factory just indexes them by role.
 */
@Component
public class DashboardStrategyFactory {

    private final Map<UserRole, DashboardStrategy> strategiesByRole;

    public DashboardStrategyFactory(List<DashboardStrategy> strategies) {
        this.strategiesByRole = new EnumMap<>(UserRole.class);
        for (DashboardStrategy strategy : strategies) {
            strategiesByRole.put(strategy.getRole(), strategy);
        }
    }

    public DashboardStrategy getStrategy(UserRole role) {
        DashboardStrategy strategy = strategiesByRole.get(role);
        if (strategy == null) {
            throw new IllegalArgumentException("No dashboard strategy registered for role: " + role);
        }
        return strategy;
    }
}
