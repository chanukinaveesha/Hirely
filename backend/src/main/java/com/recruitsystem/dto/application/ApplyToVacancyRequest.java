package com.recruitsystem.dto.application;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyToVacancyRequest {

    @NotNull
    private Long vacancyId;
}
