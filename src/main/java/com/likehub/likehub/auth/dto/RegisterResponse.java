package com.likehub.likehub.auth.dto;

import java.util.UUID;

// Devolve token de autenticação e informações públicas do usuário após o registro bem-sucedido
public record RegisterResponse(
        UUID publicId,
        String email,
        String username,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
