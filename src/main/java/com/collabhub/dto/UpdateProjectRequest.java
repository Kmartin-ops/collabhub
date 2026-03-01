package com.collabhub.dto;

public record UpdateProjectRequest(
        String name,
        String description,
        String status
) {}