package com.recruitsystem.dashboard.dto;

import com.recruitsystem.dto.assessment.CandidateAssessmentResponse;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.entity.application.ApplicationStatus;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CandidateDashboardSection implements DashboardSection {

    private Map<ApplicationStatus, Long> applicationCountsByStatus;
    private List<InterviewResponse> upcomingInterviews;
    private List<CandidateAssessmentResponse> assignedAssessments;
    private boolean resumeOnFile;
    private String resumeFileName;

    @Override
    public void applyTo(DashboardResponse.DashboardResponseBuilder builder) {
        builder.candidate(this);
    }
}
