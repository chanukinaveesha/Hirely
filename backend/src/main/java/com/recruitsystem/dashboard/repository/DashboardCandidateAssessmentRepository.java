package com.recruitsystem.dashboard.repository;

import com.recruitsystem.entity.assessment.CandidateAssessment;
import com.recruitsystem.entity.assessment.CandidateAssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Read-only aggregate query for the HR dashboard tile ("assessments to
 * evaluate"). No existing service/repository exposes this across all of an
 * HR user's assessments, so this is added here rather than touching
 * CandidateAssessmentRepository.
 */
public interface DashboardCandidateAssessmentRepository extends JpaRepository<CandidateAssessment, Long> {

    long countByAssessment_CreatedBy_IdAndStatus(Long hrUserId, CandidateAssessmentStatus status);
}
