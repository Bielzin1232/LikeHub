package com.likehub.likehub.comments.dto;

import java.util.UUID;

public record CommentReplyRequest(
        String text,
        UUID commentId
) {
}
