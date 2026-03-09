package com.collabhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request body for creating a new user")
public record CreateUserRequest(

        @Schema(description = "Full name", example = "Alice Smith") @NotBlank(message = "Name is required") String name,

        @Schema(description = "Unique email address", example = "alice@collabhub.com") @NotBlank(message = "Email is required") @Email(message = "Must be a valid email address") String email,

        @Schema(description = "User role", example = "DEVELOPER", allowableValues = {
                "ADMIN", "MANAGER",
                "DEVELOPER" }) @NotBlank(message = "Role is required") @Pattern(regexp = "ADMIN|MANAGER|DEVELOPER", message = "Role must be ADMIN, MANAGER, or DEVELOPER") String role,

        @Schema(description = "User password", example = "password123") @NotBlank(message = "Password is required") String password) {
}
