package com.collabhub;

import com.collabhub.exception.DuplicateResourceException;
import com.collabhub.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Custom Exceptions")
class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("ResourceNotFoundException should carry 404 status")
    void resourceNotFoundShouldBe404() {
        var ex = new ResourceNotFoundException("Project", "abc-123");
        assertEquals(404, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Project"));
        assertTrue(ex.getMessage().contains("abc-123"));
    }

    @Test
    @DisplayName("DuplicateResourceException should carry 409 status")
    void duplicateShouldBe409() {
        var ex = new DuplicateResourceException("User", "alice@test.com");
        assertEquals(409, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("User"));
    }

    @Test
    @DisplayName("ResourceNotFoundException message should name the resource")
    void shouldIncludeResourceName() {
        var ex = new ResourceNotFoundException("Task", "task-id-999");
        assertTrue(ex.getMessage().contains("Task not found"));
    }
}