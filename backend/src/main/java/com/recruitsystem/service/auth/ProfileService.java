package com.recruitsystem.service.auth;

import com.recruitsystem.dto.auth.ChangePasswordRequest;
import com.recruitsystem.dto.auth.DeactivateAccountRequest;
import com.recruitsystem.dto.auth.ProfileResponse;
import com.recruitsystem.dto.auth.UpdateProfileRequest;

public interface ProfileService {

    ProfileResponse getProfile(Long userId);

    ProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void deactivateAccount(Long userId, DeactivateAccountRequest request);
}
