package com.example.motels.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new motel.
 *
 * Validation Rules:
 * - motelChainId: required, valid UUID format
 * - motelName: required, 2-100 characters
 * - status: optional, must be ACTIVE or INACTIVE
 * - pincode: required, 5-10 characters, alphanumeric
 * - state: required, 2-50 characters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new motel")
public class CreateMotelRequest {

    @Schema(description = "ID of the motel chain (UUID format)", example = "f22bdf78-4713-4292-ac50-a2be85155115", required = true)
    @NotBlank(message = "Motel chain ID is required")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
             message = "Motel chain ID must be a valid UUID")
    private String motelChainId;

    @Schema(description = "Name of the motel", example = "Grand Plaza Hotel", required = true)
    @NotBlank(message = "Motel name is required")
    @Size(min = 2, max = 100, message = "Motel name must be between 2 and 100 characters")
    private String motelName;

    @Schema(description = "Status of the motel", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, required = false)
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
    private String status;

    @Schema(description = "Postal/ZIP code", example = "560001", required = true)
    @NotBlank(message = "Pincode is required")
    @Size(min = 5, max = 10, message = "Pincode must be between 5 and 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Pincode must be alphanumeric")
    private String pincode;

    @Schema(description = "State or province", example = "Karnataka", required = true)
    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    private String state;
}
