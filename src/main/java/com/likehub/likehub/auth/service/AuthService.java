package com.likehub.likehub.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.likehub.likehub.auth.dto.*;
import com.likehub.likehub.shared.security.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.likehub.likehub.shared.exceptions.ConflitoException;
import com.likehub.likehub.shared.exceptions.NaoAutenticadoException;
import com.likehub.likehub.shared.exceptions.RequisicaoInvalidaException;
import com.likehub.likehub.users.User;
import com.likehub.likehub.users.UserRepository;

@Service
public class AuthService {

    private static final Set<String> RESERVADOS = Set.of(
            "me", "search", "api", "auth", "login", "logout", "register",
            "posts", "comments", "likes", "feed", "explore", "notifications", "media",
            "admin", "administrator", "root", "support", "suporte", "help", "staff",
            "moderator", "security", "likehub", "official", "null", "undefined");

    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    private final String hashFalso;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.hashFalso = passwordEncoder.encode("nenhum-usuario-tem-esta-senha-" + UUID.randomUUID());
        this.refreshTokenService = refreshTokenService;
    }

    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase(Locale.ROOT);
        String username = request.username().toLowerCase(Locale.ROOT);

        if (excedeLimiteDoBcrypt(request.password())) {
            throw new RequisicaoInvalidaException("SENHA_LONGA_DEMAIS",
                    "A senha excede o limite de 72 bytes (acentos e emojis ocupam mais de um byte).");
        }
        if (RESERVADOS.contains(username)) {
            throw new RequisicaoInvalidaException("USERNAME_INDISPONIVEL",
                    "Este nome de usuário não está disponível.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflitoException("EMAIL_EM_USO", "O email informado já está em uso.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ConflitoException("USERNAME_EM_USO", "O nome de usuário informado já está em uso.");
        }

        User user = User.criar(email, username, passwordEncoder.encode(request.password()));

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw traduzir(ex);
        }

        String token = jwtUtil.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, null);
        return new RegisterResponse(user.getPublicId(), user.getEmail(), user.getUsername(),
                token, refreshToken, "Bearer", jwtUtil.getExpirationMillis() / 1000);
    }


    public LoginResponse login(LoginRequest request) {
        if (excedeLimiteDoBcrypt(request.password())) {
            throw credenciaisInvalidas();
        }

        String identificador = request.identifier().strip().toLowerCase(Locale.ROOT);
        Optional<User> encontrado = identificador.contains("@")
                ? userRepository.findByEmail(identificador)
                : userRepository.findByUsername(identificador);

        String hash = encontrado.map(User::getPassword).orElse(hashFalso);
        boolean senhaConfere = passwordEncoder.matches(request.password(), hash);

        if (encontrado.isEmpty() || !senhaConfere || !encontrado.get().isEnabled()) {
            throw credenciaisInvalidas();
        }
        User user = encontrado.get();

        if (passwordEncoder.upgradeEncoding(user.getPassword())) {
            user.atualizarSenhaHash(passwordEncoder.encode(request.password()));
            userRepository.save(user);
        }
        String refreshToken = refreshTokenService.createRefreshToken(user, null);
        String token = jwtUtil.generateAccessToken(user);
        return new LoginResponse(user.getPublicId(), user.getUsername(), token, refreshToken,
                "Bearer", jwtUtil.getExpirationMillis() / 1000);
    }

    public ChangePasswordResponse changePassword(User usuarioLogado, ChangePassword changePassword) {
        if (excedeLimiteDoBcrypt(changePassword.newPassword())) {
            throw new RequisicaoInvalidaException("SENHA_LONGA_DEMAIS",
                    "A senha excede o limite de 72 bytes (acentos e emojis ocupam mais de um byte).");
        }
        if (!passwordEncoder.matches(changePassword.oldPassword(), usuarioLogado.getPassword())) {
            throw new NaoAutenticadoException("SENHA_ATUAL_INVALIDA", "A senha atual informada está incorreta.");
        }
        if (passwordEncoder.matches(changePassword.newPassword(), usuarioLogado.getPassword())) {
            throw new RequisicaoInvalidaException("SENHA_NOVA_IGUAL_A_ANTIGA", "A nova senha não pode ser igual à senha antiga.");
        }
        usuarioLogado.atualizarSenhaHash(passwordEncoder.encode(changePassword.newPassword()));
        usuarioLogado.invalidarSessoes();
        userRepository.save(usuarioLogado);
        refreshTokenService.revokeAllForUser(usuarioLogado.getPublicId());

        String refreshToken = refreshTokenService.createRefreshToken(usuarioLogado, null);
        String token = jwtUtil.generateAccessToken(usuarioLogado);
        return new ChangePasswordResponse("sucess","Senha alterada com sucesso", token, refreshToken);
    }

    public void logoutAll(User usuarioLogado) {
        usuarioLogado.invalidarSessoes();
        userRepository.save(usuarioLogado);
        refreshTokenService.revokeAllForUser(usuarioLogado.getPublicId());
    }


    private NaoAutenticadoException credenciaisInvalidas() {
        return new NaoAutenticadoException("CREDENCIAIS_INVALIDAS", "Email/usuário ou senha inválidos.");
    }

    private boolean excedeLimiteDoBcrypt(String senha) {
        return senha.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES;
    }

    private RuntimeException traduzir(DataIntegrityViolationException ex) {
        String causa = String.valueOf(ex.getMostSpecificCause().getMessage());
        if (causa.contains("uk_users_email")) {
            return new ConflitoException("EMAIL_EM_USO", "O email informado já está em uso.");
        }
        if (causa.contains("uk_users_username")) {
            return new ConflitoException("USERNAME_EM_USO", "O nome de usuário informado já está em uso.");
        }
        return new ConflitoException("CONFLITO_DE_DADOS", "Não foi possível concluir o cadastro.");
    }
}