package com.collabhub.exception;

public class CollabHubException extends RuntimeException {

    private final int statusCode;

    public CollabHubException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}