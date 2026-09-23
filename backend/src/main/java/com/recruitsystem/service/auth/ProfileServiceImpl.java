package com.recruitsystem.service.auth;

import com.recruitsystem.dto.auth.ChangePasswordRequest;
import com.recruitsystem.dto.auth.DeactivateAccountRequest;
import com.recruitsystem.dto.auth.ProfileResponse;
import com.recruitsystem.dto.auth.UpdateProfileRequest;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.HrExecutive;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.auth.User;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        return toProfileResponse(findUser(userId));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        return toProfileResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId, DeactivateAccountRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Account is already inactive");
        }
        user.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    // Recruiters and HR executives optionally belong to a client company; everyone else doesn't.
    private ProfileResponse toProfileResponse(User user) {
        Long companyId = null;
        String companyName = null;

        if (user instanceof Recruiter recruiter && recruiter.getClientCompany() != null) {
            companyId = recruiter.getClientCompany().getId();
            companyName = recruiter.getClientCompany().getCompanyName();
        } else if (user instanceof HrExecutive hrExecutive && hrExecutive.getClientCompany() != null) {
            companyId = hrExecutive.getClientCompany().getId();
            companyName = hrExecutive.getClientCompany().getCompanyName();
        }

        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .clientCompanyId(companyId)
                .clientCompanyName(companyName)
                .build();
    }
}
