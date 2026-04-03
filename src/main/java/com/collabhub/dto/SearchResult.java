package com.collabhub.dto;

import java.util.UUID;

public record SearchResult(
        UUID    id,
        String  type,       // "TASK" or "PROJECT"
        String  title,
        String  subtitle,   // status, priority, project name
        String  url         // frontend navigation hint
) {
}
