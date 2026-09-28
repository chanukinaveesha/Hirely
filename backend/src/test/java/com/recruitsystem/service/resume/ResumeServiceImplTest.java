package com.recruitsystem.service.resume;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recruitsystem.dto.resume.ResumeDownloadResponse;
import com.recruitsystem.dto.resume.ResumeResponse;
import com.recruitsystem.entity.application.Application;
import com.recruitsystem.entity.application.ApplicationStatus;
import com.recruitsystem.entity.auth.AccountStatus;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.auth.Recruiter;
import com.recruitsystem.entity.company.ClientCompany;
import com.recruitsystem.entity.resume.Resume;
import com.recruitsystem.entity.resume.ResumeFileType;
import com.recruitsystem.entity.vacancy.JobVacancy;
import com.recruitsystem.entity.vacancy.VacancyCategory;
import com.recruitsystem.entity.vacancy.VacancyStatus;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.exception.ValidationException;
import com.recruitsystem.repository.application.ApplicationRepository;
import com.recruitsystem.repository.auth.JobSeekerRepository;
import com.recruitsystem.repository.resume.ResumeRepository;
import com.recruitsystem.service.storage.FileStorageService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private JobSeekerRepository jobSeekerRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private Resource loadedResource;

    private ResumeServiceImpl resumeService;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeServiceImpl(resumeRepository, jobSeekerRepository, applicationRepository, fileStorageService);
    }

    private JobSeeker jobSeeker() {
        return JobSeeker.builder()
                .id(1L)
                .name("Sam Seeker")
                .email("sam@example.com")
                .passwordHash("hashed")
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    private Resume existingResume() {
        return Resume.builder()
                .id(50L)
                .jobSeeker(jobSeeker())
                .storedFilename("old-stored.pdf")
                .originalFilename("old-resume.pdf")
                .fileType(ResumeFileType.PDF)
                .fileSizeBytes(1000)
                .build();
    }

    @Test
    void uploadOrReplace_createsNewResumeWhenNoneExists() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "content".getBytes());
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker()));
        when(resumeRepository.findByJobSeekerId(1L)).thenReturn(Optional.empty());
        when(fileStorageService.store(eq(file), anyString())).thenReturn("new-stored.pdf");
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResumeResponse response = resumeService.uploadOrReplace(1L, file);

        assertThat(response.getOriginalFilename()).isEqualTo("resume.pdf");
        assertThat(response.getFileType()).isEqualTo(ResumeFileType.PDF);
        verify(fileStorageService, never()).delete(anyString(), anyString());
    }

    @Test
    void uploadOrReplace_deletesOldFileWhenReplacing() {
        MockMultipartFile file = new MockMultipartFile("file", "new-resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "content".getBytes());
        when(jobSeekerRepository.findById(1L)).thenReturn(Optional.of(jobSeeker()));
        when(resumeRepository.findByJobSeekerId(1L)).thenReturn(Optional.of(existingResume()));
        when(fileStorageService.store(eq(file), anyString())).thenReturn("new-stored.docx");
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResumeResponse response = resumeService.uploadOrReplace(1L, file);

        assertThat(response.getOriginalFilename()).isEqualTo("new-resume.docx");
        assertThat(response.getFileType()).isEqualTo(ResumeFileType.DOCX);
        verify(fileStorageService).delete("resumes", "old-stored.pdf");
    }

    @Test
    void uploadOrReplace_throwsForUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.exe", "application/octet-stream", "content".getBytes());

        assertThatThrownBy(() -> resumeService.uploadOrReplace(1L, file)).isInstanceOf(ValidationException.class);
    }

    @Test
    void uploadOrReplace_throwsForOversizedFile() {
        byte[] tooLarge = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", tooLarge);

        assertThatThrownBy(() -> resumeService.uploadOrReplace(1L, file)).isInstanceOf(ValidationException.class);
    }

    @Test
    void getMyResume_throwsWhenNoneOnFile() {
        when(resumeRepository.findByJobSeekerId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeService.getMyResume(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void downloadMyResume_returnsResourceAndMetadata() {
        when(resumeRepository.findByJobSeekerId(1L)).thenReturn(Optional.of(existingResume()));
        when(fileStorageService.load("resumes", "old-stored.pdf")).thenReturn(loadedResource);

        ResumeDownloadResponse download = resumeService.downloadMyResume(1L);

        assertThat(download.getFilename()).isEqualTo("old-resume.pdf");
        assertThat(download.getContentType()).isEqualTo("application/pdf");
        assertThat(download.getResource()).isSameAs(loadedResource);
    }

    private Application applicationFor(Long recruiterId) {
        ClientCompany company = ClientCompany.builder().id(10L).companyName("Acme Corp").build();
        Recruiter poster = Recruiter.builder().id(recruiterId).name("Rita").email("rita@example.com")
                .passwordHash("hashed").accountStatus(AccountStatus.ACTIVE).clientCompany(company).build();
        JobVacancy vacancy = JobVacancy.builder().id(5L).title("Backend Engineer").category(VacancyCategory.IT)
                .status(VacancyStatus.PUBLISHED).deadline(LocalDate.now().plusDays(10)).postedBy(poster)
                .clientCompany(company).build();
        return Application.builder().id(100L).jobSeeker(jobSeeker()).vacancy(vacancy)
                .status(ApplicationStatus.SUBMITTED).build();
    }

    @Test
    void downloadForApplication_throwsWhenCallerDoesNotOwnVacancy() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(applicationFor(2L)));

        assertThatThrownBy(() -> resumeService.downloadForApplication(999L, 100L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void downloadForApplication_succeedsForOwningRecruiter() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(applicationFor(2L)));
        when(resumeRepository.findByJobSeekerId(1L)).thenReturn(Optional.of(existingResume()));
        when(fileStorageService.load("resumes", "old-stored.pdf")).thenReturn(loadedResource);

        ResumeDownloadResponse download = resumeService.downloadForApplication(2L, 100L);

        assertThat(download.getFilename()).isEqualTo("old-resume.pdf");
    }
}
