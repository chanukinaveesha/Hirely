package com.recruitsystem.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
@AllArgsConstructor
public class PostImageResponse {

    private Resource resource;
    private String contentType;
}
