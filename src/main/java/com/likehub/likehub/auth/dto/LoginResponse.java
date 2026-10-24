package com.likehub.likehub.auth.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record LoginResponse(
        UUID publicId,
        String username,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}