package com.example.motels.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing MotelChain.
 * Contains the same validations as CreateMotelChainRequest.
 * ID comes from the path parameter, not the request body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating an existing motel chain")
public class UpdateMotelChainRequest {
    
    @Schema(description = "Name of the motel chain", example = "Hilton Hotels", required = true)
    @NotBlank(message = "Motel chain name is required")
    @Size(min = 2, max = 100, message = "Motel chain name must be between 2 and 100 characters")
    private String motelChainName;
    
    @Schema(description = "Display name for UI", example = "Hilton", required = false)
    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;
    
    @Schema(description = "State where the chain is headquartered", example = "California", required = true)
    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    private String state;
    
    @Schema(description = "Postal code (5-10 digits)", example = "90210", required = true)
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{5,10}$", message = "Pincode must be 5-10 digits")
    private String pincode;
    
    @Schema(description = "Status of the motel chain", example = "ACTIVE", required = true)
    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;
    
    @Schema(description = "Address details of the chain headquarters", required = false)
    @Valid
    private AddressRequest address;
    
    @Schema(description = "Contact information for the chain", required = false)
    @Valid
    private ContactInfoRequest contactInfo;
}
