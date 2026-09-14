package com.recruitsystem.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.recruitsystem.dto.auth.AuthResponse;
import com.recruitsystem.dto.auth.RegisterRequest;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.exception.DuplicateResourceException;
import com.recruitsystem.repository.auth.HrExecutiveRepository;
import com.recruitsystem.repository.auth.InterviewPanelMemberRepository;
import com.recruitsystem.repository.auth.JobSeekerRepository;
import com.recruitsystem.repository.auth.RecruiterRepository;
import com.recruitsystem.repository.auth.SystemAdministratorRepository;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.company.ClientCompanyRepository;
import com.recruitsystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JobSeekerRepository jobSeekerRepository;
    @Mock
    private RecruiterRepository recruiterRepository;
    @Mock
    private HrExecutiveRepository hrExecutiveRepository;
    @Mock
    private InterviewPanelMemberRepository interviewPanelMemberRepository;
    @Mock
    private SystemAdministratorRepository systemAdministratorRepository;
    @Mock
    private ClientCompanyRepository clientCompanyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                jobSeekerRepository,
                recruiterRepository,
                hrExecutiveRepository,
                interviewPanelMemberRepository,
                systemAdministratorRepository,
                clientCompanyRepository,
                passwordEncoder,
                jwtService,
                authenticationManager);
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setName("Jane Doe");
        request.setPassword("password123");
        request.setRole(UserRole.JOB_SEEKER);

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void register_createsJobSeekerAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new.seeker@example.com");
        request.setName("Jane Doe");
        request.setPassword("password123");
        request.setPhone("0770000000");
        request.setRole(UserRole.JOB_SEEKER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(jobSeekerRepository.save(any(JobSeeker.class))).thenAnswer(invocation -> {
            JobSeeker jobSeeker = invocation.getArgument(0);
            jobSeeker.setId(1L);
            return jobSeeker;
        });
        when(jwtService.generateToken(anyString(), any())).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getRole()).isEqualTo(UserRole.JOB_SEEKER);
    }
}
