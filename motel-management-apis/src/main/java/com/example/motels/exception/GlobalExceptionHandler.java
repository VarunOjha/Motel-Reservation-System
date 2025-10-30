package com.example.motels.exception;

import com.example.motels.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all REST controllers.
 * Centralizes error handling and provides consistent API response format.
 * 
 * Benefits:
 * - DRY principle: No repetitive try-catch in controllers
 * - Consistent error format across all endpoints
 * - Easy to add new exception types
 * - Separation of concerns: Business logic vs error handling
 * 
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handles Bean Validation errors (@Valid, @NotBlank, etc.)
     * Returns HTTP 400 with detailed field-level errors.
     * 
     * Example response:
     * {
     *   "status": "400",
     *   "data": null,
     *   "message": "Validation failed",
     *   "errors": {
     *     "motelChainName": "Motel chain name is required",
     *     "pincode": "Pincode must be 5-10 digits"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        
        // Extract field-specific validation errors
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                errors,
                "Validation failed"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles ResourceNotFoundException (custom exception).
     * Returns HTTP 404 when entity not found.
     * 
     * Example: GET /motelChains/invalid-id
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        ApiResponse<Object> response = new ApiResponse<>(
                String.valueOf(HttpStatus.NOT_FOUND.value()),
                null,
                ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handles DuplicateResourceException (custom exception).
     * Returns HTTP 409 (Conflict) when resource already exists.
     * 
     * Example: Creating MotelChain with duplicate name+state+pincode
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        
        ApiResponse<Object> response = new ApiResponse<>(
                String.valueOf(HttpStatus.CONFLICT.value()),
                null,
                ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handles all other uncaught exceptions.
     * Returns HTTP 500 for unexpected errors.
     * 
     * IMPORTANT: Never expose internal error details to clients in production!
     * Log the full exception server-side for debugging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        // TODO: Add proper logging here (SLF4J/Logback)
        // log.error("Unexpected error occurred", ex);
        
        ApiResponse<Object> response = new ApiResponse<>(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                null,
                "An unexpected error occurred. Please contact support."
                // In development, you might want: ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
