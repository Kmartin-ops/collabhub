package com.collabhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "Request body for updating a task")
public record UpdateTaskRequest(

        @Schema(description = "New title") String title,

        @Schema(description = "New status", allowableValues = {
                "BACKLOG", "IN_PROGRESS", "IN_REVIEW",
                "DONE" }) @Pattern(regexp = "BACKLOG|IN_PROGRESS|IN_REVIEW|DONE", message = "Status must be BACKLOG, IN_PROGRESS, IN_REVIEW, or DONE") String status,

        @Schema(description = "New priority", allowableValues = { "LOW", "MEDIUM",
                "HIGH" }) @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Priority must be LOW, MEDIUM, or HIGH") String priority,

        @Schema(description = "New due date") LocalDate dueDate,

        @Schema(description = "Assignee email") @Email(message = "Assignee email must be valid") String assigneeEmail) {
}
