package com.recruitsystem.repository.interview;

import com.recruitsystem.entity.interview.Interview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationIdOrderByScheduledAtDesc(Long applicationId);

    List<Interview> findByApplication_JobSeeker_IdOrderByScheduledAtDesc(Long jobSeekerId);

    List<Interview> findByPanelMembers_IdOrderByScheduledAtAsc(Long panelMemberId);
}
