package com.recruitsystem.service.interview;

import com.recruitsystem.dto.interview.CandidateRescheduleRequest;
import com.recruitsystem.dto.interview.InterviewFeedbackResponse;
import com.recruitsystem.dto.interview.InterviewResponse;
import com.recruitsystem.dto.interview.PanelMemberSummaryResponse;
import com.recruitsystem.dto.interview.ProposeInterviewRequest;
import com.recruitsystem.dto.interview.RescheduleInterviewRequest;
import com.recruitsystem.dto.interview.SubmitFeedbackRequest;
import java.util.List;

public interface InterviewService {

    InterviewResponse propose(Long recruiterId, ProposeInterviewRequest request);

    InterviewResponse reschedule(Long recruiterId, Long interviewId, RescheduleInterviewRequest request);

    InterviewResponse cancel(Long recruiterId, Long interviewId);

    InterviewResponse accept(Long jobSeekerId, Long interviewId);

    InterviewResponse requestReschedule(Long jobSeekerId, Long interviewId, CandidateRescheduleRequest request);

    List<InterviewResponse> getMyInterviews(Long jobSeekerId);

    List<InterviewResponse> getInterviewsForApplication(Long recruiterId, Long applicationId);

    List<InterviewResponse> getMyPanelInterviews(Long panelMemberId);

    List<PanelMemberSummaryResponse> listPanelMembers();

    InterviewFeedbackResponse submitFeedback(Long panelMemberId, Long interviewId, SubmitFeedbackRequest request);

    List<InterviewFeedbackResponse> getFeedbackForInterview(Long recruiterId, Long interviewId);
}
