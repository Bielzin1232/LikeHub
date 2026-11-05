package com.likehub.likehub.comments.dto;

import com.likehub.likehub.comments.Comment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String text,
        UUID userId,
        Instant createdAt,
        List<CommentResponse> replies
) implements java.io.Serializable {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getUser().getPublicId(),
                comment.getCreatedAt(),
                List.of()
        );
    }
}