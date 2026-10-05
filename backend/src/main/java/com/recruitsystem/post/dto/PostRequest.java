package com.recruitsystem.post.dto;

import com.recruitsystem.post.entity.PostType;
import lombok.Getter;
import lombok.Setter;

// Bound via @ModelAttribute from a multipart form (type, body, vacancyId are
// plain fields; the image file is a separate controller parameter). Fields
// are validated manually in PostServiceImpl, same as the file-upload
// validation elsewhere in this app — @ModelAttribute binding failures don't
// flow through the same exception path as @RequestBody @Valid.
@Getter
@Setter
public class PostRequest {

    private PostType type;
    private String body;
    private Long vacancyId;
}
