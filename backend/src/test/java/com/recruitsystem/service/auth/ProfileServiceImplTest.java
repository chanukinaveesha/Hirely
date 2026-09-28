package com.recruitsystem.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.recruitsystem.dto.auth.ChangePasswordRequest;
import com.recruitsystem.dto.auth.DeactivateAccountRequest;
import com.recruitsystem.dto.auth.ProfileResponse;
import com.recruitsystem.dto.auth.UpdateProfileRequest;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.HrExecutive;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.company.ClientCompany;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.auth.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ProfileServiceImpl profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileServiceImpl(userRepository, passwordEncoder);
    }

    private JobSeeker jobSeeker() {
        return JobSeeker.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .passwordHash("hashed")
                .phone("0770000000")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void getProfile_throwsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProfile_includesClientCompanyForHrExecutive() {
        ClientCompany company = ClientCompany.builder().id(5L).companyName("Acme Corp").build();
        HrExecutive hr = HrExecutive.builder()
                .id(2L)
                .name("Hank R")
                .email("hank@example.com")
                .passwordHash("hashed")
                .accountStatus(AccountStatus.ACTIVE)
                .clientCompany(company)
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(hr));

        ProfileResponse response = profileService.getProfile(2L);

        assertThat(response.getClientCompanyId()).isEqualTo(5L);
        assertThat(response.getClientCompanyName()).isEqualTo("Acme Corp");
    }

    @Test
    void updateProfile_updatesNameAndPhone() {
        JobSeeker jobSeeker = jobSeeker();
        when(userRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Jane Updated");
        request.setPhone("0779999999");

        ProfileResponse response = profileService.updateProfile(1L, request);

        assertThat(response.getName()).isEqualTo("Jane Updated");
        assertThat(response.getPhone()).isEqualTo("0779999999");
    }

    @Test
    void changePassword_throwsWhenCurrentPasswordWrong() {
        JobSeeker jobSeeker = jobSeeker();
        when(userRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("newpassword123");

        assertThatThrownBy(() -> profileService.changePassword(1L, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void changePassword_updatesHashWhenCurrentPasswordCorrect() {
        JobSeeker jobSeeker = jobSeeker();
        when(userRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(passwordEncoder.matches("oldpass", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("new-hashed");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpass");
        request.setNewPassword("newpassword123");

        profileService.changePassword(1L, request);

        assertThat(jobSeeker.getPasswordHash()).isEqualTo("new-hashed");
    }

    @Test
    void deactivateAccount_setsStatusInactiveWhenPasswordCorrect() {
        JobSeeker jobSeeker = jobSeeker();
        when(userRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);

        DeactivateAccountRequest request = new DeactivateAccountRequest();
        request.setCurrentPassword("correct");

        profileService.deactivateAccount(1L, request);

        assertThat(jobSeeker.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void deactivateAccount_throwsWhenAlreadyInactive() {
        JobSeeker jobSeeker = jobSeeker();
        jobSeeker.setAccountStatus(AccountStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);

        DeactivateAccountRequest request = new DeactivateAccountRequest();
        request.setCurrentPassword("correct");

        assertThatThrownBy(() -> profileService.deactivateAccount(1L, request))
                .isInstanceOf(ValidationException.class);
    }
}
