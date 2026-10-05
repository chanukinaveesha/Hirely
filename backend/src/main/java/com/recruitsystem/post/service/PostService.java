package com.recruitsystem.post.service;

import com.recruitsystem.post.dto.PostFeedResponse;
import com.recruitsystem.post.dto.PostImageResponse;
import com.recruitsystem.post.dto.PostRequest;
import com.recruitsystem.post.dto.PostResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {

    PostResponse create(Long authorId, PostRequest request, MultipartFile image);

    PostFeedResponse getFeed(int page, int size);

    /** Used by the dashboard's "latest posts" widget for every role. */
    List<PostResponse> getRecent(int limit);

    void delete(Long userId, Long postId);

    PostImageResponse getImage(Long postId);
}
