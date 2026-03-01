package com.collabhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Task data returned by the API")
public record TaskResponse(

        @Schema(description = "Unique identifier")
        UUID id,

        @Schema(description = "Task title", example = "Build login page")
        String title,

        @Schema(description = "Current status", example = "IN_PROGRESS",
                allowableValues = {"BACKLOG", "IN_PROGRESS", "IN_REVIEW", "DONE"})
        String status,

        @Schema(description = "Priority", example = "HIGH",
                allowableValues = {"LOW", "MEDIUM", "HIGH"})
        String priority,

        @Schema(description = "Due date", example = "2024-12-31")
        LocalDate dueDate,

        @Schema(description = "Project this task belongs to")
        UUID projectId,

        @Schema(description = "Project name")
        String projectName,

        @Schema(description = "Assigned user ID")
        UUID assigneeId,

        @Schema(description = "Assigned user name")
        String assigneeName,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}