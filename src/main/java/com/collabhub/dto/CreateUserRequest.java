package com.collabhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "Role is required")
        @Pattern(
                regexp = "ADMIN|MANAGER|DEVELOPER",
                message = "Role must be ADMIN, MANAGER, or DEVELOPER"
        )
        String role
) {}