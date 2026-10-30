package com.likehub.likehub.likes;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LikeToggleRequest(@NotNull UUID postId) {}
