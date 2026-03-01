package com.collabhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a new project")
public record CreateProjectRequest(

        @Schema(description = "Project name", example = "CollabHub MVP")
        @NotBlank(message = "Project name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @Schema(description = "Project description", example = "Core platform features")
        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Schema(description = "Email of the project creator", example = "alice@collabhub.com")
        @NotBlank(message = "Creator email is required")
        @Email(message = "Creator email must be valid")
        String creatorEmail
) {}