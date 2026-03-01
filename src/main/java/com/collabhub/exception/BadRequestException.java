package com.collabhub.exception;

public class BadRequestException extends CollabHubException {

    public BadRequestException(String message) {
        super(message, 400);
    }
}