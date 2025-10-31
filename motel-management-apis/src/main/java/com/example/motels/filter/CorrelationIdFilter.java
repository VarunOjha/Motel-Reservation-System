package com.example.motels.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * MDC Filter for Correlation ID Management
 * 
 * THEORY: What is a Filter?
 * ========================
 * A filter is like a security checkpoint at an airport:
 * - Every request passes through it BEFORE reaching the controller
 * - You can inspect, modify, or add information to the request
 * - Executes in order (we use @Order(1) to run first)
 * 
 * THEORY: What is OncePerRequestFilter?
 * ====================================
 * Spring's special filter that guarantees execution EXACTLY ONCE per request
 * - Even if request is forwarded/included multiple times
 * - Prevents duplicate processing
 * 
 * THEORY: Why @Order(1)?
 * ======================
 * - Filters run in numerical order: @Order(1), @Order(2), @Order(3)...
 * - We want correlation ID set FIRST, before any other processing
 * - Like putting on a name tag before entering a building
 * 
 * MDC THEORY:
 * ===========
 * MDC = Mapped Diagnostic Context (from SLF4J)
 * - Thread-local storage: Each thread has its own "backpack"
 * - Put data in: MDC.put("key", "value")
 * - Automatically available in all logs from that thread
 * - Must clear at end: MDC.clear() to prevent memory leaks
 * 
 * FLOW:
 * =====
 * 1. Request arrives → Filter intercepts
 * 2. Extract or generate correlation ID
 * 3. Put ID in MDC (thread's backpack)
 * 4. Add ID to response header (so client can track)
 * 5. Continue to controller/service
 * 6. All logs automatically include correlation ID
 * 7. Finally: Clear MDC when request completes
 */
@Component
@Order(1)  // Run FIRST before all other filters
public class CorrelationIdFilter extends OncePerRequestFilter {
    
    // Constants - like road signs (standardize names)
    private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";
    
    /**
     * Main filter method - called for EVERY HTTP request
     * 
     * @param request  - Incoming HTTP request
     * @param response - Outgoing HTTP response
     * @param filterChain - Next filter/servlet in chain
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // STEP 1: Get or Generate Correlation ID
            // =======================================
            // Check if client already sent a correlation ID in header
            // If not, generate a new one (UUID = Universally Unique Identifier)
            
            String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
            
            if (correlationId == null || correlationId.trim().isEmpty()) {
                // No correlation ID from client → Generate new UUID
                correlationId = UUID.randomUUID().toString();
                
                // THEORY: Why UUID?
                // - Globally unique (no collisions even across servers)
                // - Format: 8-4-4-4-12 hex digits (e.g., "550e8400-e29b-41d4-a716-446655440000")
                // - Perfect for tracking requests across microservices
            }
            
            // STEP 2: Put Correlation ID in MDC
            // ==================================
            // CRITICAL: This makes correlation ID available to ALL logs in this request
            
            MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
            
            // THEORY: What just happened?
            // - Current thread: http-nio-8085-exec-1
            // - MDC internal storage: Thread[exec-1] → { correlationId: "abc-123" }
            // - Now ALL logs from this thread will see "abc-123"
            
            // STEP 3: Add Correlation ID to Response Header
            // =============================================
            // Send correlation ID back to client in response header
            // Client can use this ID when reporting errors or asking for support
            
            response.setHeader(CORRELATION_ID_HEADER_NAME, correlationId);
            
            // THEORY: Why send back to client?
            // Example: User gets error → sends correlation ID to support
            // Support searches logs: grep "abc-123" logs/*.log
            // Finds exact request flow that caused the error!
            
            // STEP 4: Continue Request Processing
            // ===================================
            // Pass request to next filter/controller
            // MDC is already set, so all subsequent logs will include correlation ID
            
            filterChain.doFilter(request, response);
            
            // THEORY: Execution Flow
            // filterChain.doFilter() → Controller → Service → Database
            // All logs have correlation ID from MDC
            // Then returns here ↓
            
        } finally {
            // STEP 5: Clean Up MDC
            // ====================
            // CRITICAL: Must clear MDC when request finishes
            
            MDC.clear();
            
            // THEORY: Why clear?
            // - Tomcat uses thread pools (threads are reused)
            // - Without clear: Thread-1 processes Request-A (correlationId: abc)
            //                  Then processes Request-B (no clear)
            //                  Request-B logs show "abc" → WRONG! 🐛
            // - With clear: Each request starts fresh ✅
            
            // THEORY: Why finally block?
            // - Executes EVEN IF exception occurs
            // - Guarantees MDC cleanup in all scenarios
            // - Prevents memory leaks and wrong correlation IDs
        }
    }
    
    /**
     * THEORY: Filter Lifecycle
     * ========================
     * 
     * HTTP Request Flow with MDC:
     * 
     * 1. Request arrives at Tomcat
     *    ↓
     * 2. CorrelationIdFilter.doFilterInternal() called
     *    - correlationId = "abc-123"
     *    - MDC.put("correlationId", "abc-123")
     *    ↓
     * 3. filterChain.doFilter() → Next in chain
     *    ↓
     * 4. Spring Security filters (if any)
     *    ↓
     * 5. DispatcherServlet → Controller
     *    - log.info("Request received") → [correlationId: abc-123] Request received
     *    ↓
     * 6. Service layer
     *    - log.info("Creating hotel") → [correlationId: abc-123] Creating hotel
     *    ↓
     * 7. Database operation
     *    - log.debug("SQL query") → [correlationId: abc-123] SQL query
     *    ↓
     * 8. Return back through chain
     *    ↓
     * 9. finally { MDC.clear() }
     *    - Clear correlation ID from thread
     *    ↓
     * 10. Response sent to client
     *     - Header: X-Correlation-Id: abc-123
     * 
     * 
     * THEORY: Thread Pool Visualization
     * =================================
     * 
     * Without MDC.clear():
     * ────────────────────
     * Thread-1: Request-A (abc) → Service → MDC still has "abc"
     * Thread-1: Request-B (xyz) → Service → MDC shows "abc" 🐛 WRONG!
     * 
     * With MDC.clear():
     * ────────────────
     * Thread-1: Request-A (abc) → Service → MDC.clear() ✅
     * Thread-1: Request-B (xyz) → Service → MDC has "xyz" ✅ CORRECT!
     */
}
