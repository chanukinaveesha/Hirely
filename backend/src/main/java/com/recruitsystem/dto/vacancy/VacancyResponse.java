package com.recruitsystem.dto.vacancy;

import com.recruitsystem.entity.vacancy.VacancyCategory;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VacancyResponse {

    private Long id;
    private String title;
    private String description;
    private String requirements;
    private VacancyCategory category;
    private String location;
    private LocalDate deadline;
    private VacancyStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long postedByUserId;
    private String postedByName;

    private Long clientCompanyId;
    private String clientCompanyName;
}
