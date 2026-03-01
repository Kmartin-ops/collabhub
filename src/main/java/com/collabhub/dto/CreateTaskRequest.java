package com.collabhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request body for creating a new task")
public record CreateTaskRequest(

        @Schema(description = "Task title", example = "Build login page")
        @NotBlank(message = "Title is required")
        String title,

        @Schema(description = "Priority", example = "HIGH",
                allowableValues = {"LOW", "MEDIUM", "HIGH"})
        @NotBlank(message = "Priority is required")
        @Pattern(regexp = "LOW|MEDIUM|HIGH",
                message = "Priority must be LOW, MEDIUM, or HIGH")
        String priority,

        @Schema(description = "Due date", example = "2024-12-31")
        @NotNull(message = "Due date is required")
        @Future(message = "Due date must be in the future")
        LocalDate dueDate,

        @Schema(description = "Project ID this task belongs to")
        @NotNull(message = "Project ID is required")
        UUID projectId,

        @Schema(description = "Creator email")
        @NotBlank(message = "Creator email is required")
        @Email(message = "Creator email must be valid")
        String creatorEmail
) {}