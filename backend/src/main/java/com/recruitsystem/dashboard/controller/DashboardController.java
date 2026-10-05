package com.recruitsystem.dashboard.controller;

import com.recruitsystem.dashboard.dto.DashboardResponse;
import com.recruitsystem.dashboard.service.DashboardService;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        UserRole role = UserRole.valueOf(
                principal.getAuthority().getAuthority().substring(ROLE_AUTHORITY_PREFIX.length()));
        return ResponseEntity.ok(dashboardService.getDashboard(principal.getId(), role));
    }
}
