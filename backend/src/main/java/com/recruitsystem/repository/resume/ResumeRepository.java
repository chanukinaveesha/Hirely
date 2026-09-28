package com.recruitsystem.repository.resume;

import com.recruitsystem.entity.resume.Resume;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByJobSeekerId(Long jobSeekerId);
}
