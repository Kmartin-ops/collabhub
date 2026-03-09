package com.collabhub.exception;

public class DuplicateResourceException extends CollabHubException {

    public DuplicateResourceException(String resource, Object identifier) {
        super(resource + " already exists: " + identifier, 409);
    }
}
