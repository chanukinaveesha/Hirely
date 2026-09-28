package com.recruitsystem.dto.resume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

/** Carries a loaded file plus the metadata the controller needs to set response headers. */
@Getter
@Builder
@AllArgsConstructor
public class ResumeDownloadResponse {

    private Resource resource;
    private String filename;
    private String contentType;
}
