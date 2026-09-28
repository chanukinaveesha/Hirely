package com.recruitsystem.dto.auth;

import com.recruitsystem.dto.company.CreateClientCompanyRequest;
import com.recruitsystem.entity.auth.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private String phone;

    @NotNull
    private UserRole role;

    /**
     * Only meaningful for RECRUITER and HR_EXECUTIVE roles, which must
     * provide exactly one of these two: either the id of an existing
     * company, or the details to create a new one.
     */
    private Long clientCompanyId;

    @Valid
    private CreateClientCompanyRequest newClientCompany;
}
