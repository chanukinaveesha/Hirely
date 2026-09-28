package com.recruitsystem.dto.company;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Fills in a brand new ClientCompany inline, e.g. from the registration form. */
@Getter
@Setter
public class CreateClientCompanyRequest {

    @NotBlank
    private String companyName;

    private String industry;
    private String description;
    private String address;
    private String phone;
    private String email;
}
