package com.example.motels.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Standard API response wrapper for all endpoints.
 * Provides consistent response format across the application.
 * 
 * THEORY: Why Wrapper Classes?
 * ===========================
 * Instead of returning raw data:
 *   return hotel;  ← Just the data, no metadata
 * 
 * We wrap in ApiResponse:
 *   return ApiResponse.success(hotel);  ← Data + status + correlationId + timestamp
 * 
 * Benefits:
 * - Consistent format across ALL APIs
 * - Correlation ID for request tracking
 * - Timestamp for debugging
 * - Status code for client logic
 * - Message for errors/warnings
 * 
 * MDC INTEGRATION:
 * ===============
 * This class now uses MDC.get("correlationId") instead of generating new UUIDs.
 * Flow:
 * 1. Filter sets: MDC.put("correlationId", "abc-123")
 * 2. Controller: ApiResponse.success(data)
 * 3. ApiResponse reads: MDC.get("correlationId") → "abc-123"
 * 4. Response: { correlationId: "abc-123", ... }
 * 5. Logs show: [abc-123] all operations
 * 
 * Result: Same correlation ID in logs AND response! ✅
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
    
    /**
     * Constructor: status + data only
     * 
     * THEORY: Correlation ID Source
     * ============================
     * OLD: this.correlationId = UUID.randomUUID().toString();
     *      Problem: Different ID than what's in MDC/logs
     * 
     * NEW: this.correlationId = getCorrelationIdFromMDC();
     *      Solution: Same ID as MDC (used by logs)
     * 
     * Fallback: If no MDC (e.g., scheduled job, not HTTP request)
     *          → Generate new UUID
     */
    public ApiResponse(String status, T data) {
        this.status = status;
        this.correlationId = getCorrelationIdFromMDC();  // ← CHANGED: Use MDC!
        this.timestamp = LocalDateTime.now();
        this.data = data;
        this.message = null;
    }
    
    /**
     * Constructor: status + data + message
     * 
     * Used for success with custom message or warnings
     */
    public ApiResponse(String status, T data, String message) {
        this.status = status;
        this.correlationId = getCorrelationIdFromMDC();  // ← CHANGED: Use MDC!
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
    /**
     * THEORY: Factory Methods vs Constructors
     * =======================================
     * 
     * Constructors:
     *   new ApiResponse("200", data)  ← Need to remember status codes
     * 
     * Factory Methods:
     *   ApiResponse.success(data)  ← Self-documenting, cleaner
     * 
     * Benefits:
     * - More readable: success() vs new ApiResponse("200", ...)
     * - Encapsulates logic: Don't need to know status codes
     * - Can add validation/defaults easily
     */
    
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
    
    /**
     * Helper method to get correlation ID from MDC.
     * 
     * THEORY: MDC Lifecycle
     * ====================
     * HTTP Request Thread:
     *   Filter: MDC.put("correlationId", "abc-123")
     *   Controller/Service: MDC.get("correlationId") → "abc-123" ✅
     *   Filter finally: MDC.clear()
     * 
     * Non-HTTP Thread (e.g., scheduled job):
     *   No filter → No MDC → MDC.get("correlationId") → null
     *   Fallback: Generate new UUID
     * 
     * Why String instead of UUID?
     * - MDC stores only Strings (not objects)
     * - Easier serialization to JSON
     * - Compatible with distributed tracing (Zipkin, Jaeger)
     * 
     * @return Correlation ID from MDC, or new UUID if MDC is empty
     */
    private static String getCorrelationIdFromMDC() {
        String correlationId = MDC.get("correlationId");
        
        // If no correlation ID in MDC (non-HTTP context), generate new one
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        return correlationId;
    }
}
