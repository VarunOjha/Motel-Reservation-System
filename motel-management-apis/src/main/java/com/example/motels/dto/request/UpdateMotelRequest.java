package com.example.motels.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing motel.
 *
 * All fields are optional - only provided fields will be updated.
 *
 * Validation Rules:
 * - motelName: if provided, 2-100 characters
 * - status: if provided, must be ACTIVE or INACTIVE
 * - pincode: if provided, 5-10 characters, alphanumeric
 * - state: if provided, 2-50 characters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating a motel. All fields are optional - only send fields you want to update.")
public class UpdateMotelRequest {

    @Schema(description = "Name of the motel", example = "Grand Plaza Hotel Updated", required = false)
    @Size(min = 2, max = 100, message = "Motel name must be between 2 and 100 characters")
    private String motelName;

    @Schema(description = "Status of the motel", example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, required = false)
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
    private String status;

    @Schema(description = "Postal/ZIP code", example = "560002", required = false)
    @Size(min = 5, max = 10, message = "Pincode must be between 5 and 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Pincode must be alphanumeric")
    private String pincode;

    @Schema(description = "State or province", example = "Karnataka", required = false)
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    private String state;
}
