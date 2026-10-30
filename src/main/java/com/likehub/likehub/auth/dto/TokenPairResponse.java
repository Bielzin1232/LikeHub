package com.likehub.likehub.auth.dto;

public record TokenPairResponse(
        String acessToken,
        String refreshToken,
        String tokenType
) {
    public TokenPairResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}
