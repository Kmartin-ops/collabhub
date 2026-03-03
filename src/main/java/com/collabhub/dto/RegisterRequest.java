package com.collabhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank@Email(message = "Valid email required")
        String email,
        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        @Pattern(regexp = "MANAGER|DEVELOPER|VIEWER",
        message = "Role must be MANAGER, DEVELOPER, or VIEWER")
        String role
) {
}
