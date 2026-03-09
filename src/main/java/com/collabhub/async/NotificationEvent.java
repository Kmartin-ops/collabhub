package com.collabhub.async;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationEvent(UUID eventId, String recipient, // who gets the notification
        String message, // what the notification says
        String eventType, // "TASK_CREATED", "TASK_ASSIGNED", "TASK_COMPLETED"
        LocalDateTime occurredAt) {
    // Compact constructor — runs validation before the record is created
    public NotificationEvent {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient cannot be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be blank");
        }
    }

    // Factory method — cleaner than calling the constructor directly
    public static NotificationEvent of(String recipient, String message, String eventType) {
        return new NotificationEvent(UUID.randomUUID(), recipient, message, eventType, LocalDateTime.now());
    }
}
