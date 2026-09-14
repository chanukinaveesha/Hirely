package com.recruitsystem.service.auth;

import com.recruitsystem.dto.auth.AuthResponse;
import com.recruitsystem.dto.auth.LoginRequest;
import com.recruitsystem.dto.auth.RegisterRequest;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.HrExecutive;
import com.recruitsystem.entity.auth.InterviewPanelMember;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.auth.SystemAdministrator;
import com.recruitsystem.entity.auth.User;
import com.recruitsystem.entity.company.ClientCompany;
import com.recruitsystem.exception.DuplicateResourceException;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.repository.auth.HrExecutiveRepository;
import com.recruitsystem.repository.auth.InterviewPanelMemberRepository;
import com.recruitsystem.repository.auth.JobSeekerRepository;
import com.recruitsystem.repository.auth.RecruiterRepository;
import com.recruitsystem.repository.auth.SystemAdministratorRepository;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.company.ClientCompanyRepository;
import com.recruitsystem.security.JwtService;
import com.recruitsystem.security.UserPrincipal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final RecruiterRepository recruiterRepository;
    private final HrExecutiveRepository hrExecutiveRepository;
    private final InterviewPanelMemberRepository interviewPanelMemberRepository;
    private final SystemAdministratorRepository systemAdministratorRepository;
    private final ClientCompanyRepository clientCompanyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = buildUserForRole(request);
        User saved = persist(user);

        String token = issueToken(saved);
        return toAuthResponse(saved, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + request.getEmail()));

        String token = issueToken(user);
        return toAuthResponse(user, token);
    }

    private User buildUserForRole(RegisterRequest request) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        return switch (request.getRole()) {
            case JOB_SEEKER -> JobSeeker.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .passwordHash(hashedPassword)
                    .phone(request.getPhone())
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();
            case RECRUITER -> Recruiter.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .passwordHash(hashedPassword)
                    .phone(request.getPhone())
                    .accountStatus(AccountStatus.ACTIVE)
                    .clientCompany(resolveClientCompany(request.getClientCompanyId()))
                    .build();
            case HR_EXECUTIVE -> HrExecutive.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .passwordHash(hashedPassword)
                    .phone(request.getPhone())
                    .accountStatus(AccountStatus.ACTIVE)
                    .clientCompany(resolveClientCompany(request.getClientCompanyId()))
                    .build();
            case INTERVIEW_PANEL_MEMBER -> InterviewPanelMember.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .passwordHash(hashedPassword)
                    .phone(request.getPhone())
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();
            case SYSTEM_ADMINISTRATOR -> SystemAdministrator.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .passwordHash(hashedPassword)
                    .phone(request.getPhone())
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();
        };
    }

    private ClientCompany resolveClientCompany(Long clientCompanyId) {
        if (clientCompanyId == null) {
            return null;
        }
        return clientCompanyRepository.findById(clientCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Client company not found: " + clientCompanyId));
    }

    private User persist(User user) {
        return switch (user) {
            case JobSeeker jobSeeker -> jobSeekerRepository.save(jobSeeker);
            case Recruiter recruiter -> recruiterRepository.save(recruiter);
            case HrExecutive hrExecutive -> hrExecutiveRepository.save(hrExecutive);
            case InterviewPanelMember panelMember -> interviewPanelMemberRepository.save(panelMember);
            case SystemAdministrator admin -> systemAdministratorRepository.save(admin);
            default -> throw new IllegalStateException("Unsupported user subtype: " + user.getClass());
        };
    }

    private String issueToken(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        return jwtService.generateToken(
                principal.getUsername(),
                Map.of(
                        "userId", user.getId(),
                        "role", user.getRole().name()));
    }

    private AuthResponse toAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
