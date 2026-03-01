package com.collabhub.dto;

public record CreateUserRequest(
        String name,
        String email,
        String role
) {}