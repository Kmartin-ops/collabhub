package com.collabhub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        String originalFileName,
        String contentType,
        long fileSize,
        String uploadedBy,
        LocalDateTime uploadedAt,
        String downloadUrl
) {}
