package com.likehub.likehub.comments.dto;

import java.util.UUID;

public record CreateCommentResponse(
        String status,
        UUID commentId
) {
}
