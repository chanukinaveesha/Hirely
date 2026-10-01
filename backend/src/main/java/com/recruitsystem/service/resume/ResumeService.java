package com.recruitsystem.service.resume;

import com.recruitsystem.dto.resume.ResumeDownloadResponse;
import com.recruitsystem.dto.resume.ResumeResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {

    ResumeResponse uploadOrReplace(Long jobSeekerId, MultipartFile file);

    ResumeResponse getMyResume(Long jobSeekerId);

    ResumeDownloadResponse downloadMyResume(Long jobSeekerId);

    ResumeDownloadResponse downloadForApplication(Long recruiterId, Long applicationId);
}
