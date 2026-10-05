package com.recruitsystem.dashboard.service;

import com.recruitsystem.dashboard.dto.DashboardResponse;
import com.recruitsystem.entity.auth.UserRole;

public interface DashboardService {

    DashboardResponse getDashboard(Long userId, UserRole role);
}
