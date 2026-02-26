package com.project.dscatalog.services.exceptions;

public class ResourceEntityNotFoundException extends RuntimeException {
    public ResourceEntityNotFoundException(String message) {
        super(message);
    }
}
