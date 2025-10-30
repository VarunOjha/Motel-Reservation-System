package com.example.motels.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Standard API response wrapper for all endpoints.
 * Provides consistent response format across the application.
 * 
 * @param <T> Type of data being returned
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private String status;
    private String correlationId;
    private LocalDateTime timestamp;
    private T data;
    private String message;
    
    // Constructors
    public ApiResponse() {}
    
    public ApiResponse(String status, T data) {
        this.status = status;
        this.correlationId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.data = data;
        this.message = null;
    }
    
    public ApiResponse(String status, T data, String message) {
        this.status = status;
        this.correlationId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.data = data;
        this.message = message;
    }
    
    public ApiResponse(String status, String correlationId, LocalDateTime timestamp, T data, String message) {
        this.status = status;
        this.correlationId = correlationId;
        this.timestamp = timestamp;
        this.data = data;
        this.message = message;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    // Static factory methods for convenience
    public static <T> ApiResponse<T> success(String status, T data) {
        return new ApiResponse<>(status, data);
    }
    
    public static <T> ApiResponse<T> success(String status, T data, String message) {
        return new ApiResponse<>(status, data, message);
    }
    
    public static <T> ApiResponse<T> error(String status, String message) {
        return new ApiResponse<>(status, null, message);
    }
    
    public static <T> ApiResponse<T> error(String status, T data, String message) {
        return new ApiResponse<>(status, data, message);
    }
}
