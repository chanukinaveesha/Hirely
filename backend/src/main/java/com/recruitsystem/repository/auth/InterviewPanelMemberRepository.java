package com.recruitsystem.repository.auth;

import com.recruitsystem.entity.auth.InterviewPanelMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewPanelMemberRepository extends JpaRepository<InterviewPanelMember, Long> {
}
