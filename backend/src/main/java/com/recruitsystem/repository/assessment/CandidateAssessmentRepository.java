package com.recruitsystem.repository.assessment;

import com.recruitsystem.entity.assessment.CandidateAssessment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateAssessmentRepository extends JpaRepository<CandidateAssessment, Long> {

    boolean existsByAssessmentIdAndApplicationId(Long assessmentId, Long applicationId);

    List<CandidateAssessment> findByApplication_JobSeeker_IdOrderByDeadlineAsc(Long jobSeekerId);

    List<CandidateAssessment> findByApplicationIdOrderByAssignedAtDesc(Long applicationId);

    List<CandidateAssessment> findByApplication_Vacancy_IdOrderByScoreDesc(Long vacancyId);
}
