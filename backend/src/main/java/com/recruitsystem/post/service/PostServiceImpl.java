package com.recruitsystem.post.service;

import com.recruitsystem.avatar.service.AvatarService;
import com.recruitsystem.entity.auth.User;
import com.recruitsystem.entity.auth.UserRole;
import com.recruitsystem.entity.vacancy.JobVacancy;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.media.ImageContentType;
import com.recruitsystem.media.ImageValidationUtils;
import com.recruitsystem.post.dto.PostFeedResponse;
import com.recruitsystem.post.dto.PostImageResponse;
import com.recruitsystem.post.dto.PostRequest;
import com.recruitsystem.post.dto.PostResponse;
import com.recruitsystem.post.entity.Post;
import com.recruitsystem.post.entity.PostType;
import com.recruitsystem.post.repository.PostRepository;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.vacancy.JobVacancyRepository;
import com.recruitsystem.service.storage.FileStorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final int MAX_BODY_LENGTH = 2000;
    private static final String STORAGE_SUBDIRECTORY = "post-images";

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final JobVacancyRepository jobVacancyRepository;
    private final FileStorageService fileStorageService;
    private final AvatarService avatarService;

    @Override
    @Transactional
    public PostResponse create(Long authorId, PostRequest request, MultipartFile image) {
        validateBody(request.getBody());
        if (request.getType() == null) {
            throw new ValidationException("Post type is required");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authorId));

        JobVacancy vacancy = resolveVacancyForRole(author, request);

        Post.PostBuilder postBuilder = Post.builder()
                .author(author)
                .type(request.getType())
                .body(request.getBody())
                .vacancy(vacancy);

        if (image != null && !image.isEmpty()) {
            ImageContentType imageType = ImageValidationUtils.validate(image);
            postBuilder
                    .imageStoredFilename(fileStorageService.store(image, STORAGE_SUBDIRECTORY))
                    .imageOriginalFilename(image.getOriginalFilename())
                    .imageContentType(imageType.getMimeType());
        }

        return toResponse(postRepository.save(postBuilder.build()));
    }

    @Override
    @Transactional(readOnly = true)
    public PostFeedResponse getFeed(int page, int size) {
        Page<Post> result = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return PostFeedResponse.builder()
                .items(result.getContent().stream().map(this::toResponse).toList())
                .page(page)
                .size(size)
                .totalElements(result.getTotalElements())
                .hasMore(result.hasNext())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getRecent(int limit) {
        return postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).getContent().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        if (!post.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own posts");
        }
        if (post.getImageStoredFilename() != null) {
            fileStorageService.delete(STORAGE_SUBDIRECTORY, post.getImageStoredFilename());
        }
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostImageResponse getImage(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        if (post.getImageStoredFilename() == null) {
            throw new ResourceNotFoundException("This post has no image");
        }
        Resource resource = fileStorageService.load(STORAGE_SUBDIRECTORY, post.getImageStoredFilename());
        return PostImageResponse.builder()
                .resource(resource)
                .contentType(post.getImageContentType())
                .build();
    }

    private void validateBody(String body) {
        if (body == null || body.isBlank()) {
            throw new ValidationException("Post body must not be empty");
        }
        if (body.length() > MAX_BODY_LENGTH) {
            throw new ValidationException("Post body must be " + MAX_BODY_LENGTH + " characters or fewer");
        }
    }

    private JobVacancy resolveVacancyForRole(User author, PostRequest request) {
        UserRole role = author.getRole();

        if (role == UserRole.JOB_SEEKER) {
            if (request.getType() != PostType.PROFILE_SHOWCASE) {
                throw new ValidationException("Job seekers can only create profile showcase posts");
            }
            if (request.getVacancyId() != null) {
                throw new ValidationException("Profile showcase posts can't reference a vacancy");
            }
            return null;
        }

        if (role == UserRole.RECRUITER || role == UserRole.HR_EXECUTIVE) {
            if (request.getType() != PostType.VACANCY_PROMO) {
                throw new ValidationException("Recruiters and HR can only create vacancy promo posts");
            }
            if (request.getVacancyId() == null) {
                throw new ValidationException("Vacancy promo posts must reference a vacancy");
            }
            JobVacancy vacancy = jobVacancyRepository.findById(request.getVacancyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: " + request.getVacancyId()));
            if (!vacancy.getPostedBy().getId().equals(author.getId())) {
                throw new UnauthorizedException("You can only promote your own vacancies");
            }
            if (vacancy.getStatus() != VacancyStatus.PUBLISHED) {
                throw new ValidationException("Only published vacancies can be promoted");
            }
            return vacancy;
        }

        throw new UnauthorizedException("Your role cannot create posts");
    }

    private PostResponse toResponse(Post post) {
        User author = post.getAuthor();
        boolean isVacancyPromo = post.getType() == PostType.VACANCY_PROMO;

        return PostResponse.builder()
                .id(post.getId())
                .type(post.getType())
                .body(post.getBody())
                .authorId(author.getId())
                .authorName(author.getName())
                .authorRole(author.getRole())
                .authorAvatarUrl(avatarService.findImageUrl(author.getId()).orElse(null))
                .imageUrl(post.getImageStoredFilename() != null ? "/posts/" + post.getId() + "/image" : null)
                .linkUrl(isVacancyPromo ? "/vacancies/" + post.getVacancy().getId() : "/people/" + author.getId())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
