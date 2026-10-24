package com.likehub.likehub.auth.controller;

import com.likehub.likehub.auth.commons.api.ApiPaths;
import com.likehub.likehub.auth.dto.*;
import com.likehub.likehub.auth.service.AuthService;
import com.likehub.likehub.auth.service.RefreshTokenService;
import com.likehub.likehub.shared.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(ApiPaths.Auth.REGISTER)
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping(ApiPaths.Auth.LOGIN)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(ApiPaths.Auth.REFRESH)
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshTokenService.RotatedToken rotated =
                refreshTokenService.rotate(request.refreshToken());

        String newAccess = jwtUtil.generateAccessToken(rotated.user());
        return ResponseEntity.ok(new RefreshResponse(newAccess, rotated.refreshToken()));
    }

    @PostMapping(ApiPaths.Auth.LOGOUT)
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revokeByRawToken(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
