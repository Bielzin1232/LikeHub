package com.likehub.likehub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//Esse vai utilizar o método já autenticado o outro vai ser atraves do esqueci minha senha
public record ChangePassword(
        @NotBlank(message = "A senha antiga é obrigatória")
        @Size(min = 8, max = 255, message = "A senha deve ter entre 8 e 255 caracteres")
        String oldPassword,
        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 8, max = 255, message = "A senha deve ter entre 8 e 255 caracteres")
        String newPassword
) {
}
