package com.likehub.likehub.comments.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
        @NotBlank(message = "O texto não pode ser vazio")
        String text
) {
}