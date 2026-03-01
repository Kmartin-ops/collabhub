package com.collabhub.dto;

public record CreateProjectRequest(
        String name,
        String description,
        String creatorEmail  // we look up the User by email
) {}