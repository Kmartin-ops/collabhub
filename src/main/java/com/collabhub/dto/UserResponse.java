package com.collabhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "User data returned by the API")
public record UserResponse(

        @Schema(description = "Unique identifier")
        UUID id,

        @Schema(description = "Full name", example = "Alice Smith")
        String name,

        @Schema(description = "Email address", example = "alice@collabhub.com")
        String email,

        @Schema(description = "Role", example = "MANAGER")
        String role,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}