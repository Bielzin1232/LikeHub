package com.likehub.likehub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * O identificador de login deve ser o email, que é único por conta.
 */
public record LoginRequest(
        @NotBlank @Size(max = 255)
        String identifier,

        @NotBlank @Size(max = 72)
        String password
) {
        @Override
        public String toString() {
                return "LoginRequest[identifier=" + identifier + ", password=***]";
        }
}