package com.example.motels.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Results in HTTP 409 (Conflict) response.
 * 
 * Example: Creating a MotelChain with same name, state, pincode combination.
 */
public class DuplicateResourceException extends RuntimeException {
    
    private final String resourceName;
    private final String details;
    
    public DuplicateResourceException(String resourceName, String details) {
        super(String.format("%s already exists: %s", resourceName, details));
        this.resourceName = resourceName;
        this.details = details;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public String getDetails() {
        return details;
    }
}
