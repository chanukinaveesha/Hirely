package com.recruitsystem.dashboard.dto;

import com.recruitsystem.post.dto.PostResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// Exactly one of the role sections is non-null, matching the signed-in
// user's role. recentPosts is always present — every role sees the latest
// posts widget.
@Getter
@Builder
@AllArgsConstructor
public class DashboardResponse {

    private CandidateDashboardSection candidate;
    private RecruiterDashboardSection recruiter;
    private HrDashboardSection hr;
    private PanelDashboardSection panel;
    private AdminDashboardSection admin;

    private List<PostResponse> recentPosts;
}
