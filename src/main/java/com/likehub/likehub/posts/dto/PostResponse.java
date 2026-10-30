package com.likehub.likehub.posts.dto;
import com.likehub.likehub.posts.Post;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String title,
        String content,
        Instant createdAt,
        List<ImageDto> images
) {
    public static PostResponse from(Post p) {
        return new PostResponse(
                p.getId(), p.getTitle(), p.getContent(), p.getCreatedAt(),
                p.getImages().stream()
                        .map(i -> new ImageDto(i.getUrl(), i.getPosition()))
                        .toList()
        );
    }
    public record ImageDto(String url, int position) {}
}