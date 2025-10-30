package com.example.motels.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Address input in requests.
 * Nested in MotelChain create/update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;
    
    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;
    
    @Size(max = 255, message = "Landmark must not exceed 255 characters")
    private String landmark;
    
    @Size(max = 100, message = "Address name must not exceed 100 characters")
    private String addressName;
    
    @NotBlank(message = "Address status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;
}
