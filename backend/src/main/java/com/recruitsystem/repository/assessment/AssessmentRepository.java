package com.recruitsystem.repository.assessment;

import com.recruitsystem.entity.assessment.Assessment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByCreatedByIdOrderByCreatedAtDesc(Long createdByUserId);
}
