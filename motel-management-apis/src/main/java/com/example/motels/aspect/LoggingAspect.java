package com.example.motels.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for automatic logging of service and controller methods.
 * 
 * This aspect provides:
 * - Automatic method entry/exit logging
 * - Performance tracking (execution time)
 * - Exception logging with stack traces
 * - Argument and return value logging (in DEBUG/TRACE)
 * 
 * Benefits over manual logging:
 * - Zero clutter in business logic
 * - Consistent logging format across all services
 * - Automatic for all methods (no risk of forgetting)
 * - Easy to modify (change once, applies everywhere)
 * - Reusable across multiple services
 * 
 * Best Practices Followed:
 * 1. Log levels: INFO for key events, DEBUG for details, ERROR for exceptions
 * 2. Performance tracking: Automatic duration measurement
 * 3. Selective logging: Arguments/results only in DEBUG/TRACE to avoid performance impact
 * 4. Exception handling: Re-throw after logging to maintain normal flow
 * 5. Clean output: Structured format with emojis for quick visual scanning
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {
    
    /**
     * Pointcut matching all public methods in service layer.
     * Pattern: com.example.motels.service.*Service.*(..)
     * 
     * Currently matches:
     * - MotelChainService.createMotelChain()
     * - MotelChainService.getMotelChainById()
     * - MotelChainService.updateMotelChain()
     * - MotelChainService.deleteMotelChain()
     * 
     * Will automatically apply to any future services you create!
     */
    @Pointcut("within(com.example.motels.service..*) && execution(public * *(..))")
    public void serviceMethods() {}
    
    /**
     * Pointcut matching all public methods in controller layer.
     * Pattern: com.example.motels.controller.*Controller.*(..)
     */
    @Pointcut("within(com.example.motels.controller..*) && execution(public * *(..))")
    public void controllerMethods() {}
    
    /**
     * Around advice for SERVICE layer methods.
     * 
     * Logs:
     * - Entry: Method name and arguments (DEBUG level)
     * - Exit: Method name, duration, and return value (INFO/TRACE level)
     * - Exception: Error details with duration (ERROR level)
     * 
     * Performance Impact: Minimal
     * - Only INFO logs in production
     * - DEBUG/TRACE only when explicitly enabled
     * - Duration tracking: ~microseconds overhead
     */
    @Around("serviceMethods()")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();
        
        // Entry log (DEBUG level - detailed)
        if (log.isDebugEnabled()) {
            log.debug("→ [{}] Entering: {}()", className, methodName);
            
            // Log arguments only if present and DEBUG is enabled
            if (args.length > 0) {
                // Truncate long arguments to avoid log bloat
                String argsString = Arrays.toString(args);
                if (argsString.length() > 200) {
                    argsString = argsString.substring(0, 197) + "...";
                }
                log.debug("   Arguments: {}", argsString);
            }
        }
        
        long startTime = System.currentTimeMillis();
        Object result = null;
        
        try {
            // Execute the actual method
            result = joinPoint.proceed();
            
            // Success log (INFO level - always shown)
            long duration = System.currentTimeMillis() - startTime;
            
            // Different log messages based on method type
            if (methodName.startsWith("create")) {
                log.info("✓ [{}] Created successfully in {}ms", className, duration);
            } else if (methodName.startsWith("update")) {
                log.info("✓ [{}] Updated successfully in {}ms", className, duration);
            } else if (methodName.startsWith("delete")) {
                log.info("✓ [{}] Deleted successfully in {}ms", className, duration);
            } else if (methodName.startsWith("get") || methodName.startsWith("find")) {
                log.info("← [{}] Retrieved data in {}ms", className, duration);
            } else {
                log.info("← [{}] {} completed in {}ms", className, methodName, duration);
            }
            
            // Log return value only in TRACE level (very verbose)
            if (log.isTraceEnabled() && result != null) {
                String resultString = result.toString();
                if (resultString.length() > 200) {
                    resultString = resultString.substring(0, 197) + "...";
                }
                log.trace("   Return value: {}", resultString);
            }
            
            // Performance warning for slow operations (>1 second)
            if (duration > 1000) {
                log.warn("⚠ [{}] Slow operation detected: {}ms", className, duration);
            }
            
            return result;
            
        } catch (Exception e) {
            // Exception log (ERROR level)
            long duration = System.currentTimeMillis() - startTime;
            
            // Log exception with context
            log.error("✗ [{}] Exception in {}() after {}ms: {}", 
                className, 
                methodName, 
                duration, 
                e.getMessage());
            
            // Log stack trace only in DEBUG (avoid log flooding)
            if (log.isDebugEnabled()) {
                log.debug("   Stack trace:", e);
            }
            
            // Re-throw to maintain normal exception handling
            throw e;
        }
    }
    
    /**
     * Around advice for CONTROLLER layer methods.
     * 
     * Logs:
     * - HTTP request received
     * - HTTP response sent with status
     * - Request duration
     * 
     * This complements service logging by tracking HTTP-level metrics.
     */
    @Around("controllerMethods()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        
        // HTTP request log
        log.info("🌐 [{}] HTTP Request: {}", className, methodName);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute the controller method
            Object result = joinPoint.proceed();
            
            // HTTP response log
            long duration = System.currentTimeMillis() - startTime;
            log.info("✓ [{}] HTTP Response: {} ({}ms)", className, methodName, duration);
            
            // Warn if HTTP request is slow (>2 seconds)
            if (duration > 2000) {
                log.warn("⚠ [{}] Slow HTTP request: {}ms", className, duration);
            }
            
            return result;
            
        } catch (Exception e) {
            // HTTP error log
            long duration = System.currentTimeMillis() - startTime;
            log.error("✗ [{}] HTTP Error in {}: {} ({}ms)", 
                className, methodName, e.getMessage(), duration);
            throw e;
        }
    }
}
