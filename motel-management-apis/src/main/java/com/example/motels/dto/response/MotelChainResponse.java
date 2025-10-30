package com.example.motels.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for MotelChain in API responses.
 * Contains all fields to be exposed to clients.
 * Excludes sensitive/internal fields like deletedAt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotelChainResponse {
    
    private UUID motelChainId;
    private String motelChainName;
    private String displayName;
    private String state;
    private String pincode;
    private String status;
    
    private AddressResponse address;
    private ContactInfoResponse contactInfo;
    
    /**
     * Timestamps are included for:
     * - Debugging purposes
     * - Client-side caching (Last-Modified headers)
     * - Audit trail for users
     * 
     * Note: If security is a concern (exposing creation patterns),
     * these can be removed or restricted based on user role.
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
