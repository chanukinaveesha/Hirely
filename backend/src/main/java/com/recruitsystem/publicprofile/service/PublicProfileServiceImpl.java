package com.recruitsystem.publicprofile.service;

import com.recruitsystem.avatar.service.AvatarService;
import com.recruitsystem.entity.auth.HrExecutive;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.auth.User;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.publicprofile.dto.PublicProfileResponse;
import com.recruitsystem.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicProfileServiceImpl implements PublicProfileService {

    private final UserRepository userRepository;
    private final AvatarService avatarService;

    @Override
    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return PublicProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .role(user.getRole())
                .avatarUrl(avatarService.findImageUrl(userId).orElse(null))
                .clientCompanyName(resolveClientCompanyName(user))
                .build();
    }

    private String resolveClientCompanyName(User user) {
        if (user instanceof Recruiter recruiter && recruiter.getClientCompany() != null) {
            return recruiter.getClientCompany().getCompanyName();
        }
        if (user instanceof HrExecutive hrExecutive && hrExecutive.getClientCompany() != null) {
            return hrExecutive.getClientCompany().getCompanyName();
        }
        return null;
    }
}
