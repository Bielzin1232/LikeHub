package com.likehub.likehub.auth.service;

import com.likehub.likehub.auth.commons.api.RefreshToken;
import com.likehub.likehub.auth.commons.api.RefreshTokenRepository;
import com.likehub.likehub.shared.exceptions.NaoAutenticadoException;
import com.likehub.likehub.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Duration REFRESH_TTL = Duration.ofDays(30);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public String createRefreshToken(User user, String familyId) {
        String raw = generateOpaqueToken();
        RefreshToken entity = RefreshToken.builder()
                .tokenHash(sha256(raw))
                .user(user)
                .familyId(familyId != null ? familyId : UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(REFRESH_TTL))
                .build();
        repository.save(entity);
        return raw;
    }

    @Transactional(noRollbackFor = NaoAutenticadoException.class)
    public RotatedToken rotate(String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken current = repository.findByTokenHash(hash)
                .orElseThrow(() -> new NaoAutenticadoException("Refresh token inválido."));

        if (current.isRevoked()) {
            repository.revokeFamily(current.getFamilyId(), Instant.now());
            throw new NaoAutenticadoException("Refresh token reutilizado. Sessão encerrada por segurança.");
        }

        if (current.isExpired()) {
            throw new NaoAutenticadoException("Refresh token expirado.");
        }

        String newRaw = generateOpaqueToken();
        String newHash = sha256(newRaw);

        int changed = repository.revokeIfActive(current.getId(), Instant.now(), newHash);
        if (changed == 0) {
            repository.revokeFamily(current.getFamilyId(), Instant.now());
            throw new NaoAutenticadoException("Refresh em andamento. Faça login novamente.");
        }

        RefreshToken next = RefreshToken.builder()
                .tokenHash(newHash)
                .user(current.getUser())
                .familyId(current.getFamilyId())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(REFRESH_TTL))
                .build();
        repository.save(next);

        return new RotatedToken(current.getUser(), newRaw);
    }

    @Transactional(noRollbackFor = NaoAutenticadoException.class)
    public void revokeByRawToken(String rawToken) {
        repository.findByTokenHash(sha256(rawToken))
                .ifPresent(rt -> repository.revokeFamily(rt.getFamilyId(), Instant.now()));
    }

    @Transactional
    public void revokeAllForUser(UUID userPublicId) {
        repository.revokeAllForUser(userPublicId, Instant.now());
    }

    private static String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public record RotatedToken(User user, String refreshToken) {}
}