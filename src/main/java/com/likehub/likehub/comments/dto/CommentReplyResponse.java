package com.likehub.likehub.comments.dto;

import java.util.UUID;

public record CommentReplyResponse(
        String status,
        UUID parentCommentId
) {
}
