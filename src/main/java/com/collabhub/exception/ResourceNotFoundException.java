package com.collabhub.exception;

public class ResourceNotFoundException extends CollabHubException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super(resource + " not found: " + identifier, 404);
    }
}
