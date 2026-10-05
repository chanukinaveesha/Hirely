package com.recruitsystem.publicprofile.dto;

import com.recruitsystem.entity.auth.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// Deliberately only the fields safe to show anyone signed in: no email,
// phone, or anything else private. The User/role-subtype entities have no
// headline/summary field to leak — there's simply nothing more to include.
@Getter
@Builder
@AllArgsConstructor
public class PublicProfileResponse {

    private Long id;
    private String name;
    private UserRole role;
    private String avatarUrl;
    private String clientCompanyName;
}
