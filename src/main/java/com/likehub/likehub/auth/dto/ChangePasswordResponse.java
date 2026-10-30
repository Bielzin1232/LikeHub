package com.likehub.likehub.auth.dto;

import lombok.Builder;

@Builder
public record ChangePasswordResponse(
        String status,
        String message,
        String token,
        String refreshToken
) {
}
