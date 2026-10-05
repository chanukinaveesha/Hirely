package com.recruitsystem.dashboard.repository;

import com.recruitsystem.entity.application.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Read-only aggregate query for the HR dashboard tile ("interviews to
 * schedule"). No existing service/repository exposes a "no interview yet"
 * count, so this is added here rather than touching ApplicationRepository.
 */
public interface DashboardApplicationRepository extends JpaRepository<Application, Long> {

    @Query("""
            SELECT COUNT(a) FROM Application a
            WHERE a.vacancy.postedBy.id = :userId
              AND a.status = com.recruitsystem.entity.application.ApplicationStatus.SHORTLISTED
              AND NOT EXISTS (SELECT 1 FROM Interview i WHERE i.application = a)
            """)
    long countShortlistedAwaitingInterview(@Param("userId") Long userId);
}
