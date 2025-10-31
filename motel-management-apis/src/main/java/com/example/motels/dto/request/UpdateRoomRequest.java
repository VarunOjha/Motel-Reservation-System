package com.example.motels.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for updating an existing room.
 *
 * All fields are optional - only provided fields will be updated.
 *
 * Validation Rules:
 * - roomNumber: if provided, 1-20 characters, alphanumeric
 * - floor: if provided, max 10 characters
 * - status: if provided, must be AVAILABLE, OCCUPIED, or MAINTENANCE
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating a room. All fields are optional - only send fields you want to update.")
public class UpdateRoomRequest {

    @Schema(description = "ID of the room category", example = "b57522e9-2ecb-4b5e-9480-9b3cad04d47b", required = false)
    private UUID motelRoomCategoryId;

    @Schema(description = "Room number (1-20 characters, alphanumeric)", example = "102", required = false)
    @Size(min = 1, max = 20, message = "Room number must be between 1 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Room number must be alphanumeric (letters, numbers, and hyphens only)")
    private String roomNumber;

    @Schema(description = "Floor number or identifier", example = "2", required = false)
    @Size(max = 10, message = "Floor must not exceed 10 characters")
    private String floor;

    @Schema(description = "Room status", example = "OCCUPIED", allowableValues = {"AVAILABLE", "OCCUPIED", "MAINTENANCE"}, required = false)
    @Pattern(regexp = "AVAILABLE|OCCUPIED|MAINTENANCE", message = "Status must be AVAILABLE, OCCUPIED, or MAINTENANCE")
    private String status;
}
