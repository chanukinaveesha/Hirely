package com.recruitsystem.repository.vacancy;

import com.recruitsystem.entity.vacancy.JobVacancy;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobVacancyRepository extends JpaRepository<JobVacancy, Long> {

    List<JobVacancy> findByPostedByIdOrderByCreatedAtDesc(Long postedByUserId);

    @Query("""
            SELECT v FROM JobVacancy v
            WHERE v.status = com.recruitsystem.entity.vacancy.VacancyStatus.PUBLISHED
              AND (:title IS NULL OR LOWER(v.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:category IS NULL OR v.category = :category)
              AND (:location IS NULL OR LOWER(v.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:keyword IS NULL
                   OR LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(v.requirements) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY v.publishedAt DESC
            """)
    List<JobVacancy> search(
            @Param("title") String title,
            @Param("category") VacancyCategory category,
            @Param("location") String location,
            @Param("keyword") String keyword);
}
