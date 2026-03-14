package com.collabhub.dto;

import java.time.LocalDateTime;

public record NotificationPayload(
        String type,
        String title,
        String message,
        String entityId,
        String projectId,
        LocalDateTime timestamp
) {
    public static NotificationPayload of(String type, String title, String message,
                                          String entityId, String projectId) {
        return new NotificationPayload(type, title, message, entityId, projectId, LocalDateTime.now());
    }
}
