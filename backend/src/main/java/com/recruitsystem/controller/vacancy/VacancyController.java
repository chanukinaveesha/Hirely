package com.recruitsystem.controller.vacancy;

import com.recruitsystem.dto.vacancy.VacancyRequest;
import com.recruitsystem.dto.vacancy.VacancyResponse;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import com.recruitsystem.security.UserPrincipal;
import com.recruitsystem.service.vacancy.VacancyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
@Tag(name = "Vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<VacancyResponse> createVacancy(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody VacancyRequest request) {
        VacancyResponse response = vacancyService.createVacancy(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<VacancyResponse> updateVacancy(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody VacancyRequest request) {
        return ResponseEntity.ok(vacancyService.updateVacancy(principal.getId(), id, request));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<VacancyResponse> publishVacancy(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.publishVacancy(principal.getId(), id));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<VacancyResponse> closeVacancy(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.closeVacancy(principal.getId(), id));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HR_EXECUTIVE')")
    public ResponseEntity<List<VacancyResponse>> getMyVacancies(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(vacancyService.getMyVacancies(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacancyResponse> getVacancy(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.getVacancy(principal.getId(), id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<VacancyResponse>> searchVacancies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) VacancyCategory category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(vacancyService.search(title, category, location, keyword));
    }
}
