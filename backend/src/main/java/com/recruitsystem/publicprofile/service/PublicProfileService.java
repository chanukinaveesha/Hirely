package com.recruitsystem.publicprofile.service;

import com.recruitsystem.publicprofile.dto.PublicProfileResponse;

public interface PublicProfileService {

    PublicProfileResponse getPublicProfile(Long userId);
}
