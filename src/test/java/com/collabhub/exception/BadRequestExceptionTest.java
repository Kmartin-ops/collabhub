package com.collabhub.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BadRequestException")
class BadRequestExceptionTest {

    @Test
    @DisplayName("should use status code 400 and preserve message")
    void shouldCarryStatusAndMessage() {
        BadRequestException exception = new BadRequestException("Invalid payload");

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo("Invalid payload");
    }
}
