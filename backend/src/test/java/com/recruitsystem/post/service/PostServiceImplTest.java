package com.recruitsystem.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recruitsystem.avatar.service.AvatarService;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.HrExecutive;
import com.recruitsystem.entity.auth.InterviewPanelMember;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.vacancy.JobVacancy;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.post.dto.PostRequest;
import com.recruitsystem.post.dto.PostResponse;
import com.recruitsystem.post.entity.Post;
import com.recruitsystem.post.entity.PostType;
import com.recruitsystem.post.repository.PostRepository;
import com.recruitsystem.repository.auth.UserRepository;
import com.recruitsystem.repository.vacancy.JobVacancyRepository;
import com.recruitsystem.service.storage.FileStorageService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JobVacancyRepository jobVacancyRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private AvatarService avatarService;

    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(postRepository, userRepository, jobVacancyRepository, fileStorageService, avatarService);
    }

    private JobSeeker jobSeeker() {
        return JobSeeker.builder().id(1L).name("Sam Seeker").email("sam@example.com")
                .passwordHash("hashed").accountStatus(AccountStatus.ACTIVE).build();
    }

    private Recruiter recruiter() {
        return Recruiter.builder().id(2L).name("Rita Recruiter").email("rita@example.com")
                .passwordHash("hashed").accountStatus(AccountStatus.ACTIVE).build();
    }

    private HrExecutive hrExecutive() {
        return HrExecutive.builder().id(3L).name("Hank HR").email("hank@example.com")
                .passwordHash("hashed").accountStatus(AccountStatus.ACTIVE).build();
    }

    private InterviewPanelMember panelMember() {
        return InterviewPanelMember.builder().id(4L).name("Pat Panel").email("pat@example.com")
                .passwordHash("hashed").accountStatus(AccountStatus.ACTIVE).build();
    }

    private JobVacancy publishedVacancyPostedBy(Recruiter poster) {
        return JobVacancy.builder().id(20L).title("Backend Engineer").category(VacancyCategory.IT)
                .status(VacancyStatus.PUBLISHED).deadline(LocalDate.now().plusDays(10)).postedBy(poster).build();
    }

    private PostRequest requestOf(PostType type, String body, Long vacancyId) {
        PostRequest request = new PostRequest();
        request.setType(type);
        request.setBody(body);
        request.setVacancyId(vacancyId);
        return request;
    }

    @Test
    void create_jobSeekerProfileShowcase_succeeds() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(jobSeeker()));
        when(avatarService.findImageUrl(anyLong())).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(100L);
            return post;
        });

        PostResponse response = postService.create(1L, requestOf(PostType.PROFILE_SHOWCASE, "Hello world", null), null);

        assertThat(response.getLinkUrl()).isEqualTo("/people/1");
        assertThat(response.getType()).isEqualTo(PostType.PROFILE_SHOWCASE);
    }

    @Test
    void create_jobSeekerWithVacancyPromoType_throwsValidation() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(jobSeeker()));

        assertThatThrownBy(() -> postService.create(1L, requestOf(PostType.VACANCY_PROMO, "Hello", null), null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_recruiterVacancyPromoOnOwnPublishedVacancy_succeeds() {
        Recruiter recruiter = recruiter();
        JobVacancy vacancy = publishedVacancyPostedBy(recruiter);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recruiter));
        when(jobVacancyRepository.findById(20L)).thenReturn(Optional.of(vacancy));
        when(avatarService.findImageUrl(anyLong())).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(101L);
            return post;
        });

        PostResponse response = postService.create(2L, requestOf(PostType.VACANCY_PROMO, "Check this out", 20L), null);

        assertThat(response.getLinkUrl()).isEqualTo("/vacancies/20");
    }

    @Test
    void create_recruiterPromotingSomeoneElsesVacancy_throwsUnauthorized() {
        Recruiter owner = Recruiter.builder().id(99L).name("Other").email("other@example.com")
                .passwordHash("hashed").accountStatus(AccountStatus.ACTIVE).build();
        JobVacancy vacancy = publishedVacancyPostedBy(owner);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recruiter()));
        when(jobVacancyRepository.findById(20L)).thenReturn(Optional.of(vacancy));

        assertThatThrownBy(() -> postService.create(2L, requestOf(PostType.VACANCY_PROMO, "Check this out", 20L), null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void create_recruiterPromotingUnpublishedVacancy_throwsValidation() {
        Recruiter recruiter = recruiter();
        JobVacancy vacancy = JobVacancy.builder().id(20L).title("Draft role").category(VacancyCategory.IT)
                .status(VacancyStatus.DRAFT).deadline(LocalDate.now().plusDays(10)).postedBy(recruiter).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(recruiter));
        when(jobVacancyRepository.findById(20L)).thenReturn(Optional.of(vacancy));

        assertThatThrownBy(() -> postService.create(2L, requestOf(PostType.VACANCY_PROMO, "Check this out", 20L), null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_hrExecutiveVacancyPromo_succeeds() {
        HrExecutive hr = hrExecutive();
        JobVacancy vacancy = JobVacancy.builder().id(21L).title("HR role").category(VacancyCategory.HUMAN_RESOURCES)
                .status(VacancyStatus.PUBLISHED).deadline(LocalDate.now().plusDays(10)).postedBy(hr).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(hr));
        when(jobVacancyRepository.findById(21L)).thenReturn(Optional.of(vacancy));
        when(avatarService.findImageUrl(anyLong())).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostResponse response = postService.create(3L, requestOf(PostType.VACANCY_PROMO, "Join us", 21L), null);

        assertThat(response.getAuthorRole().name()).isEqualTo("HR_EXECUTIVE");
    }

    @Test
    void create_panelMemberCannotPost() {
        when(userRepository.findById(4L)).thenReturn(Optional.of(panelMember()));

        assertThatThrownBy(() -> postService.create(4L, requestOf(PostType.PROFILE_SHOWCASE, "Hello", null), null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void create_blankBody_throwsValidation() {
        assertThatThrownBy(() -> postService.create(1L, requestOf(PostType.PROFILE_SHOWCASE, "   ", null), null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_bodyTooLong_throwsValidation() {
        String tooLong = "a".repeat(2001);

        assertThatThrownBy(() -> postService.create(1L, requestOf(PostType.PROFILE_SHOWCASE, tooLong, null), null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void delete_throwsWhenCallerDoesNotOwnPost() {
        Post post = Post.builder().id(5L).author(jobSeeker()).type(PostType.PROFILE_SHOWCASE).body("Hi").build();
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.delete(999L, 5L)).isInstanceOf(UnauthorizedException.class);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void delete_succeedsForOwnerAndCleansUpImage() {
        Post post = Post.builder().id(5L).author(jobSeeker()).type(PostType.PROFILE_SHOWCASE).body("Hi")
                .imageStoredFilename("stored.jpg").build();
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));

        postService.delete(1L, 5L);

        verify(fileStorageService).delete("post-images", "stored.jpg");
        verify(postRepository).delete(post);
    }

    @Test
    void getImage_throwsWhenPostHasNoImage() {
        Post post = Post.builder().id(5L).author(jobSeeker()).type(PostType.PROFILE_SHOWCASE).body("Hi").build();
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.getImage(5L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
