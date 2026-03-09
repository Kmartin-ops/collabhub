package com.collabhub.dto;

public record AuthResponse(

        String accessToken,   // was: token
        String refreshToken,
        String email,
        String role,
        String name
)  { }