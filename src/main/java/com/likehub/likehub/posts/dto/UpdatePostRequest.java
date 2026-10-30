package com.likehub.likehub.posts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdatePostRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank String content,
        @Size(max = 10, message = "Máximo 10 imagens por post")
        List<@NotNull UUID> imageIds
) {}
