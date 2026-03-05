package com.collabhub.controller;

import com.collabhub.dto.ErrorResponse;
import com.collabhub.exception.CollabHubException;
import com.collabhub.exception.ResourceNotFoundException;
import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // ── 1. Bean Validation failures (@Valid) ──────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Extract every field error into "fieldName: message" strings
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .sorted()
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Validation Failed",
                        request.getRequestURI(), details));
    }

    // ── 2. Resource not found (404) ───────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found",
                        request.getRequestURI(), ex.getMessage()));
    }

    // ── 3. Duplicate resource (409) ───────────────────────────
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict",
                        request.getRequestURI(), ex.getMessage()));
    }

    // ── 4. All other CollabHub exceptions ────────────────────
    @ExceptionHandler(CollabHubException.class)
    public ResponseEntity<ErrorResponse> handleCollabHub(
            CollabHubException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ErrorResponse.of(ex.getStatusCode(), "Request Failed",
                        request.getRequestURI(), ex.getMessage()));
    }

    // ── 5. Wrong type in path variable (/api/projects/not-a-uuid) ──
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = "Invalid value '" + ex.getValue()
                + "' for parameter '" + ex.getName() + "'";

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request",
                        request.getRequestURI(), message));
    }

    // ── 6. Catch-all — unexpected exceptions ─────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex,
            HttpServletRequest request) {

        // Log the real error server-side, never expose internals to client
        System.err.println("[ERROR] Unhandled exception: " + ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Internal Server Error",
                        request.getRequestURI(),
                        "An unexpected error occurred"));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request){
        log.warn("Bad request: {}",ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        LocalDateTime.now(),400,"Bad Request",
                        request.getRequestURI(), List.of(ex.getMessage())));
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {
        log.warn("Invalid state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        LocalDateTime.now(), 401, "Unauthorized",
                        request.getRequestURI(), List.of(ex.getMessage())));
    }
}