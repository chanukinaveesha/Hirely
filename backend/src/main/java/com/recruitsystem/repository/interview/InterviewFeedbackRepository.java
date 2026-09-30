package com.recruitsystem.repository.interview;

import com.recruitsystem.entity.interview.InterviewFeedback;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {

    List<InterviewFeedback> findByInterviewId(Long interviewId);

    Optional<InterviewFeedback> findByInterviewIdAndPanelMemberId(Long interviewId, Long panelMemberId);

    long countByInterviewId(Long interviewId);
}
