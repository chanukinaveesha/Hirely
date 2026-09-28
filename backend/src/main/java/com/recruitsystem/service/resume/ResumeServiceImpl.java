package com.recruitsystem.service.resume;

import com.recruitsystem.dto.resume.ResumeDownloadResponse;
import com.recruitsystem.dto.resume.ResumeResponse;
import com.recruitsystem.entity.application.Application;
import com.recruitsystem.entity.auth.JobSeeker;
import com.recruitsystem.entity.resume.Resume;
import com.recruitsystem.entity.resume.ResumeFileType;
import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.UnauthorizedException;
import com.recruitsystem.repository.application.ApplicationRepository;
import com.recruitsystem.repository.auth.JobSeekerRepository;
import com.recruitsystem.repository.resume.ResumeRepository;
import com.recruitsystem.service.storage.FileStorageService;
import com.recruitsystem.util.FileValidationUtils;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final String STORAGE_SUBDIRECTORY = "resumes";

    private final ResumeRepository resumeRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final ApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ResumeResponse uploadOrReplace(Long jobSeekerId, MultipartFile file) {
        FileValidationUtils.validate(file, ALLOWED_EXTENSIONS, MAX_FILE_SIZE_BYTES);

        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + jobSeekerId));

        ResumeFileType fileType = ResumeFileType.fromExtension(FileValidationUtils.getExtension(file.getOriginalFilename()));
        String storedFilename = fileStorageService.store(file, STORAGE_SUBDIRECTORY);

        Resume resume = resumeRepository.findByJobSeekerId(jobSeekerId).orElse(null);
        if (resume == null) {
            resume = Resume.builder().jobSeeker(jobSeeker).build();
        } else {
            fileStorageService.delete(STORAGE_SUBDIRECTORY, resume.getStoredFilename());
        }

        resume.setStoredFilename(storedFilename);
        resume.setOriginalFilename(file.getOriginalFilename());
        resume.setFileType(fileType);
        resume.setFileSizeBytes(file.getSize());

        return toResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getMyResume(Long jobSeekerId) {
        return toResponse(findResumeOrThrow(jobSeekerId));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDownloadResponse downloadMyResume(Long jobSeekerId) {
        return toDownload(findResumeOrThrow(jobSeekerId));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDownloadResponse downloadForApplication(Long recruiterId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (!application.getVacancy().getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedException("You do not have access to this application");
        }
        return toDownload(findResumeOrThrow(application.getJobSeeker().getId()));
    }

    private Resume findResumeOrThrow(Long jobSeekerId) {
        return resumeRepository.findByJobSeekerId(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("No resume on file for this candidate"));
    }

    private ResumeResponse toResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .originalFilename(resume.getOriginalFilename())
                .fileType(resume.getFileType())
                .fileSizeBytes(resume.getFileSizeBytes())
                .uploadedAt(resume.getUploadedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    private ResumeDownloadResponse toDownload(Resume resume) {
        Resource resource = fileStorageService.load(STORAGE_SUBDIRECTORY, resume.getStoredFilename());
        return ResumeDownloadResponse.builder()
                .resource(resource)
                .filename(resume.getOriginalFilename())
                .contentType(resume.getFileType().getContentType())
                .build();
    }
}
