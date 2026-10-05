package com.recruitsystem.post.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PostFeedResponse {

    private List<PostResponse> items;
    private int page;
    private int size;
    private long totalElements;
    private boolean hasMore;
}
