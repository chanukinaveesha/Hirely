package com.recruitsystem.entity.auth;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "job_seekers")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("JOB_SEEKER")
@NoArgsConstructor
@SuperBuilder
public class JobSeeker extends User {

    @Override
    public UserRole getRole() {
        return UserRole.JOB_SEEKER;
    }
}
