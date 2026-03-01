package com.collabhub.dto;

import com.collabhub.domain.Project;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        String status,
        List<String> memberNames,
        int memberCount,
        LocalDateTime createdAt
) {
    public static ProjectResponse from(Project project) {
        List<String> members = project.getMembers().stream()
                .map(u -> u.getName())
                .sorted()
                .toList();

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                members,
                members.size(),
                project.getCreatedAt()
        );
    }
}