package com.example.motels.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Results in HTTP 404 response.
 * 
 * Why custom exception instead of just returning null?
 * - Cleaner service layer code (no if-else chains)
 * - Centralized error handling in @ControllerAdvice
 * - Consistent error response format
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
}
