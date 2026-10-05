package com.recruitsystem.dashboard.dto;

import com.recruitsystem.entity.auth.UserRole;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminDashboardSection {

    private long totalUsers;
    private long totalVacancies;
    private Map<UserRole, Long> usersByRole;
}
