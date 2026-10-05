package com.recruitsystem.dashboard.strategy;

import com.recruitsystem.dashboard.dto.DashboardSection;
import com.recruitsystem.entity.auth.UserRole;

/**
 * Strategy Pattern - Strategy interface.
 * Declares the family of interchangeable algorithms for building a
 * role-specific dashboard section, plus the role each implementation handles
 * (used by DashboardStrategyFactory to select the right one at runtime).
 */
public interface DashboardStrategy {

    UserRole getRole();

    DashboardSection buildSection(Long userId);
}
