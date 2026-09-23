package com.recruitsystem.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeactivateAccountRequest {

    @NotBlank
    private String currentPassword;
}
