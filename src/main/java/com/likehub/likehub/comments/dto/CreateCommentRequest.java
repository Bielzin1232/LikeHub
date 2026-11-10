package com.likehub.likehub.comments.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
        @NotBlank(message = "O texto não pode ser vazio")
        String text,
        @NotNull(message = "O post é obrigatório")
        UUID postPublicId
) {
}
