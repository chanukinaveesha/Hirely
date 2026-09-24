package com.recruitsystem.service.vacancy;

import com.recruitsystem.dto.vacancy.VacancyRequest;
import com.recruitsystem.dto.vacancy.VacancyResponse;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import java.util.List;

public interface VacancyService {

    VacancyResponse createVacancy(Long posterId, VacancyRequest request);

    VacancyResponse updateVacancy(Long posterId, Long vacancyId, VacancyRequest request);

    VacancyResponse publishVacancy(Long posterId, Long vacancyId);

    VacancyResponse closeVacancy(Long posterId, Long vacancyId);

    VacancyResponse getVacancy(Long viewerId, Long vacancyId);

    List<VacancyResponse> getMyVacancies(Long posterId);

    List<VacancyResponse> search(String title, VacancyCategory category, String location, String keyword);
}
