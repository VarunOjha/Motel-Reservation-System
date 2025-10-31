package com.example.motels.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Address information")
public class AddressRequest {
    
    @Schema(description = "Primary address line", example = "123 Main Street", required = true)
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;
    
    @Schema(description = "Secondary address line", example = "Suite 100", required = false)
    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;
    
    @Schema(description = "Nearby landmark", example = "Near Central Park", required = false)
    @Size(max = 255, message = "Landmark must not exceed 255 characters")
    private String landmark;
    
    @Schema(description = "Name/label for this address", example = "Headquarters", required = false)
    @Size(max = 100, message = "Address name must not exceed 100 characters")
    private String addressName;
    
    @Schema(description = "Status of the address", example = "ACTIVE", required = true)
    @NotBlank(message = "Address status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;
}
