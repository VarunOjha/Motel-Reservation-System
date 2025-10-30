package com.example.motels.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new MotelChain.
 * Only contains fields that clients are allowed to provide during creation.
 * Fields like id, createdAt, updatedAt are excluded as they're server-controlled.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMotelChainRequest {
    
    @NotBlank(message = "Motel chain name is required")
    @Size(min = 2, max = 100, message = "Motel chain name must be between 2 and 100 characters")
    private String motelChainName;
    
    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;
    
    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    private String state;
    
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{5,10}$", message = "Pincode must be 5-10 digits")
    private String pincode;
    
    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;
    
    /**
     * Nested validation with @Valid ensures Address fields are validated too.
     * If address is required, add @NotNull annotation here.
     */
    @Valid
    private AddressRequest address;
    
    /**
     * Nested validation for contact information.
     * If contactInfo is required, add @NotNull annotation here.
     */
    @Valid
    private ContactInfoRequest contactInfo;
}
