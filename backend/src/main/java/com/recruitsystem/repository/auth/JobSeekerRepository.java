package com.recruitsystem.repository.auth;

import com.recruitsystem.entity.auth.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSeekerRepository extends JpaRepository<JobSeeker, Long> {
}
