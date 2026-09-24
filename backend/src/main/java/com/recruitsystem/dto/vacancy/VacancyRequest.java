package com.recruitsystem.dto.vacancy;

import com.recruitsystem.entity.vacancy.VacancyCategory;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** Used for both creating and editing a vacancy — same fields either way. */
@Getter
@Setter
public class VacancyRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String requirements;

    @NotNull
    private VacancyCategory category;

    private String location;

    @NotNull
    @FutureOrPresent(message = "Deadline must be today or later")
    private LocalDate deadline;
}
