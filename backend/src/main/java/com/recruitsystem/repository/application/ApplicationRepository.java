package com.recruitsystem.repository.application;

import com.recruitsystem.entity.application.Application;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByJobSeekerIdOrderByAppliedAtDesc(Long jobSeekerId);

    List<Application> findByVacancyIdOrderByAppliedAtDesc(Long vacancyId);

    boolean existsByJobSeekerIdAndVacancyId(Long jobSeekerId, Long vacancyId);
}
