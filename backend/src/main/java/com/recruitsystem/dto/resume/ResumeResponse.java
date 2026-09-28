package com.recruitsystem.dto.resume;

import com.recruitsystem.entity.resume.ResumeFileType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResumeResponse {

    private Long id;
    private String originalFilename;
    private ResumeFileType fileType;
    private long fileSizeBytes;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}
