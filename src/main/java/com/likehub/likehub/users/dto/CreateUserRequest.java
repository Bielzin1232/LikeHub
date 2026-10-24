package com.likehub.likehub.users.dto;

public record CreateUserRequest(
        String email,
        String password,
        String userName
) {
}
