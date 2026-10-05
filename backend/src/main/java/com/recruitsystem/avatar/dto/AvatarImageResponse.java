package com.recruitsystem.avatar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
@AllArgsConstructor
public class AvatarImageResponse {

    private Resource resource;
    private String contentType;
}
