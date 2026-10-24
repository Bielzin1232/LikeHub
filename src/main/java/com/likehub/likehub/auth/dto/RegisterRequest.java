package com.likehub.likehub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        @Size(max = 255, message = "O email deve ter no máximo 255 caracteres")
        String email,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 255, message = "A senha deve ter entre 8 e 255 caracteres")
        String password,
        @NotBlank(message = "O nome de usuário é obrigatório")
        @Size(min = 3, max = 50, message = "O nome de usuário deve ter entre 3 e 50 caracteres")
        String username
) {
}
