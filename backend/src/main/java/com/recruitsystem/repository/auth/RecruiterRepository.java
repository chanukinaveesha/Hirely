package com.recruitsystem.repository.auth;

import com.recruitsystem.entity.auth.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
}
