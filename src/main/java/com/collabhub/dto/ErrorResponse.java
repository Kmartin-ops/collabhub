package com.collabhub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String path,
        List<String> details
) {
    // Convenience constructor — single message, no field-level details
    public static ErrorResponse of(int status, String error,
                                   String path, String message) {
        return new ErrorResponse(
                LocalDateTime.now(), status, error, path, List.of(message));
    }

    // Convenience constructor — multiple field-level details
    public static ErrorResponse of(int status, String error,
                                   String path, List<String> details) {
        return new ErrorResponse(
                LocalDateTime.now(), status, error, path, details);
    }
}