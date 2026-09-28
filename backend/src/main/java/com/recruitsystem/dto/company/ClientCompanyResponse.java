package com.recruitsystem.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClientCompanyResponse {

    private Long id;
    private String companyName;
    private String industry;
}
