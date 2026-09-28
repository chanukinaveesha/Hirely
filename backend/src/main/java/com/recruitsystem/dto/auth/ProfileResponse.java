package com.recruitsystem.dto.auth;

import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private AccountStatus accountStatus;

    // Only populated for RECRUITER / HR_EXECUTIVE profiles.
    private Long clientCompanyId;
    private String clientCompanyName;
}
