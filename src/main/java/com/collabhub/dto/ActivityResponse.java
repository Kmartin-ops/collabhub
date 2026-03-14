package com.collabhub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        String action,
        String actorName,
        String entityType,
        UUID entityId,
        String entityName,
        String detail,
        LocalDateTime createdAt
) {}
