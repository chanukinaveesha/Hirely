package com.recruitsystem.dashboard.strategy;

import com.recruitsystem.dashboard.dto.DashboardSection;

/**
 * Strategy Pattern - Context.
 * Holds a reference to the current DashboardStrategy and delegates to it.
 * Deliberately NOT a Spring-managed singleton: it carries per-call mutable
 * state (the current strategy), so the client creates a fresh instance for
 * each request instead of sharing one across concurrent users.
 */
public class DashboardContext {

    private DashboardStrategy strategy;

    public void setStrategy(DashboardStrategy strategy) {
        this.strategy = strategy;
    }

    public DashboardSection execute(Long userId) {
        return strategy.buildSection(userId);
    }
}
