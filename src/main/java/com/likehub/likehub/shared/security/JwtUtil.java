package com.likehub.likehub.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import com.likehub.likehub.users.User;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMillis}")
    private long jwtExpirationMillis;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET é obrigatório.");
        }
        byte[] bytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve possuir pelo menos 32 bytes.");
        }
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(User user) {
        String username = user.getPublicId().toString();
        List<String> roles = user.getRoleNames();
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        claims.put("sessionVersion", user.getSessionVersion());
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(User user) {
        return generateToken(user);
    }

    public long getExpirationMillis() {
        return jwtExpirationMillis;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("roles");
        return roles == null ? List.of() : (List<String>) roles;
    }

    public long extractSessionVersion(String token) {
        Object version = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("sessionVersion");
        if (version instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("Token sem versão de sessão.");
    }
}