package com.collabhub.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        String email,
        String role,
        String name
) {
}
