package com.example.sdkdemo.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " not found for identifier: " + identifier);
    }
}
