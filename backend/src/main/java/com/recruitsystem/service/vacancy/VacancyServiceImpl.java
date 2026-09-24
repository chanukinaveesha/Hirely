package com.recruitsystem.service.vacancy;

import com.recruitsystem.dto.vacancy.VacancyRequest;
import com.recruitsystem.dto.vacancy.VacancyResponse;
import com.recruitsystem.entity.auth.HrExecutive;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.auth.User;
import com.recruitsystem.entity.company.ClientCompany;
import com.recruitsystem.entity.vacancy.JobVacancy;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.vacancy.JobVacancyRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

    private final JobVacancyRepository jobVacancyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VacancyResponse createVacancy(Long posterId, VacancyRequest request) {
        User poster = findUser(posterId);
        ClientCompany clientCompany = resolvePosterCompany(poster);

        JobVacancy vacancy = JobVacancy.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .category(request.getCategory())
                .location(request.getLocation())
                .deadline(request.getDeadline())
                .status(VacancyStatus.DRAFT)
                .postedBy(poster)
                .clientCompany(clientCompany)
                .build();

        return toResponse(jobVacancyRepository.save(vacancy));
    }

    @Override
    @Transactional
    public VacancyResponse updateVacancy(Long posterId, Long vacancyId, VacancyRequest request) {
        JobVacancy vacancy = findOwnedVacancy(posterId, vacancyId);
        if (vacancy.getStatus() == VacancyStatus.CLOSED) {
            throw new ValidationException("A closed vacancy cannot be edited");
        }

        vacancy.setTitle(request.getTitle());
        vacancy.setDescription(request.getDescription());
        vacancy.setRequirements(request.getRequirements());
        vacancy.setCategory(request.getCategory());
        vacancy.setLocation(request.getLocation());
        vacancy.setDeadline(request.getDeadline());

        return toResponse(jobVacancyRepository.save(vacancy));
    }

    @Override
    @Transactional
    public VacancyResponse publishVacancy(Long posterId, Long vacancyId) {
        JobVacancy vacancy = findOwnedVacancy(posterId, vacancyId);
        if (vacancy.getStatus() != VacancyStatus.DRAFT) {
            throw new ValidationException("Only a draft vacancy can be published");
        }

        vacancy.setStatus(VacancyStatus.PUBLISHED);
        vacancy.setPublishedAt(LocalDateTime.now());
        return toResponse(jobVacancyRepository.save(vacancy));
    }

    @Override
    @Transactional
    public VacancyResponse closeVacancy(Long posterId, Long vacancyId) {
        JobVacancy vacancy = findOwnedVacancy(posterId, vacancyId);
        if (vacancy.getStatus() == VacancyStatus.CLOSED) {
            throw new ValidationException("Vacancy is already closed");
        }

        vacancy.setStatus(VacancyStatus.CLOSED);
        return toResponse(jobVacancyRepository.save(vacancy));
    }

    @Override
    @Transactional(readOnly = true)
    public VacancyResponse getVacancy(Long viewerId, Long vacancyId) {
        JobVacancy vacancy = jobVacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: " + vacancyId));

        boolean isOwner = vacancy.getPostedBy().getId().equals(viewerId);
        if (vacancy.getStatus() != VacancyStatus.PUBLISHED && !isOwner) {
            // Hide drafts/closed postings from everyone except the poster.
            throw new ResourceNotFoundException("Vacancy not found: " + vacancyId);
        }

        return toResponse(vacancy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacancyResponse> getMyVacancies(Long posterId) {
        return jobVacancyRepository.findByPostedByIdOrderByCreatedAtDesc(posterId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacancyResponse> search(String title, VacancyCategory category, String location, String keyword) {
        return jobVacancyRepository.search(blankToNull(title), category, blankToNull(location), blankToNull(keyword))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private JobVacancy findOwnedVacancy(Long posterId, Long vacancyId) {
        JobVacancy vacancy = jobVacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: " + vacancyId));
        if (!vacancy.getPostedBy().getId().equals(posterId)) {
            throw new UnauthorizedException("You do not own this vacancy");
        }
        return vacancy;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    // A vacancy is always posted under the poster's own client company; there's
    // no separate company picker since Recruiter/HrExecutive already carry one.
    private ClientCompany resolvePosterCompany(User poster) {
        ClientCompany company = switch (poster) {
            case Recruiter recruiter -> recruiter.getClientCompany();
            case HrExecutive hrExecutive -> hrExecutive.getClientCompany();
            default -> null;
        };
        if (company == null) {
            throw new ValidationException("Your account is not linked to a client company yet");
        }
        return company;
    }

    private VacancyResponse toResponse(JobVacancy vacancy) {
        return VacancyResponse.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .description(vacancy.getDescription())
                .requirements(vacancy.getRequirements())
                .category(vacancy.getCategory())
                .location(vacancy.getLocation())
                .deadline(vacancy.getDeadline())
                .status(vacancy.getStatus())
                .publishedAt(vacancy.getPublishedAt())
                .createdAt(vacancy.getCreatedAt())
                .updatedAt(vacancy.getUpdatedAt())
                .postedByUserId(vacancy.getPostedBy().getId())
                .postedByName(vacancy.getPostedBy().getName())
                .clientCompanyId(vacancy.getClientCompany().getId())
                .clientCompanyName(vacancy.getClientCompany().getCompanyName())
                .build();
    }
}
