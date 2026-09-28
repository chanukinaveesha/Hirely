package com.recruitsystem.dto.application;

import com.recruitsystem.entity.application.ApplicationStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApplicationResponse {

    private Long id;
    private Long vacancyId;
    private String vacancyTitle;
    private String clientCompanyName;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    // Populated for the recruiter-facing applicant list; unused by the job seeker's own view.
    private Long jobSeekerId;
    private String jobSeekerName;
    private String jobSeekerEmail;
}
