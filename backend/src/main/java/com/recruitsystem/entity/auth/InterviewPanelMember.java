package com.recruitsystem.entity.auth;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "interview_panel_members")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("INTERVIEW_PANEL_MEMBER")
@NoArgsConstructor
@SuperBuilder
public class InterviewPanelMember extends User {

    @Override
    public UserRole getRole() {
        return UserRole.INTERVIEW_PANEL_MEMBER;
    }
}
