package com.recruitsystem.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recruitsystem.dto.auth.AuthResponse;
import com.recruitsystem.dto.auth.RegisterRequest;
import com.recruitsystem.dto.company.CreateClientCompanyRequest;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.entity.company.ClientCompany;
import com.recruitsystem.exception.DuplicateResourceException;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.auth.HrExecutiveRepository;
import com.recruitsystem.repository.auth.InterviewPanelMemberRepository;
import com.recruitsystem.repository.auth.JobSeekerRepository;
import com.recruitsystem.repository.auth.RecruiterRepository;
import com.recruitsystem.repository.auth.SystemAdministratorRepository;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.company.ClientCompanyRepository;
import com.recruitsystem.security.JwtService;
import java.util.Optional;
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

    private RegisterRequest recruiterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("rita@example.com");
        request.setName("Rita Recruiter");
        request.setPassword("password123");
        request.setRole(UserRole.RECRUITER);
        return request;
    }

    @Test
    void register_recruiterWithExistingCompany_linksIt() {
        RegisterRequest request = recruiterRequest();
        request.setClientCompanyId(10L);

        ClientCompany company = ClientCompany.builder().id(10L).companyName("Acme Corp").build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(clientCompanyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> {
            Recruiter recruiter = invocation.getArgument(0);
            recruiter.setId(1L);
            return recruiter;
        });
        when(jwtService.generateToken(anyString(), any())).thenReturn("token");

        authService.register(request);

        verify(clientCompanyRepository, never()).save(any());
    }

    @Test
    void register_recruiterWithNewCompanyDetails_createsIt() {
        RegisterRequest request = recruiterRequest();
        CreateClientCompanyRequest newCompany = new CreateClientCompanyRequest();
        newCompany.setCompanyName("Brand New Co");
        request.setNewClientCompany(newCompany);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(clientCompanyRepository.save(any(ClientCompany.class))).thenAnswer(invocation -> {
            ClientCompany company = invocation.getArgument(0);
            company.setId(20L);
            return company;
        });
        when(recruiterRepository.save(any(Recruiter.class))).thenAnswer(invocation -> {
            Recruiter recruiter = invocation.getArgument(0);
            recruiter.setId(2L);
            return recruiter;
        });
        when(jwtService.generateToken(anyString(), any())).thenReturn("token");

        authService.register(request);

        verify(clientCompanyRepository).save(argThat(company -> "Brand New Co".equals(company.getCompanyName())));
    }

    @Test
    void register_recruiterWithNeitherCompanyOption_throws() {
        RegisterRequest request = recruiterRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(ValidationException.class);
    }

    @Test
    void register_recruiterWithBothCompanyOptions_throws() {
        RegisterRequest request = recruiterRequest();
        request.setClientCompanyId(10L);
        request.setNewClientCompany(new CreateClientCompanyRequest());
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(ValidationException.class);
    }

    @Test
    void register_recruiterWithUnknownCompanyId_throwsNotFound() {
        RegisterRequest request = recruiterRequest();
        request.setClientCompanyId(999L);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(clientCompanyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(ResourceNotFoundException.class);
    }
}
