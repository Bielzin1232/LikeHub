package com.likehub.likehub.auth.dto;

public record RefreshResponse(
        String token,
        String refreshToken
) {}