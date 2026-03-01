package com.collabhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Project data returned by the API")
public record ProjectResponse(

        @Schema(description = "Unique identifier")
        UUID id,

        @Schema(description = "Project name", example = "CollabHub MVP")
        String name,

        @Schema(description = "Project description")
        String description,

        @Schema(description = "Status", example = "ACTIVE",
                allowableValues = {"ACTIVE", "COMPLETED", "ARCHIVED"})
        String status,

        @Schema(description = "Names of all project members")
        List<String> memberNames,

        @Schema(description = "Total number of members")
        int memberCount,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}