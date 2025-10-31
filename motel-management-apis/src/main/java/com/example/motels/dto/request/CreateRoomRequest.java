package com.example.motels.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new room.
 *
 * Validation Rules:
 * - motelChainId: required
 * - motelId: required
 * - motelRoomCategoryId: required
 * - roomNumber: required, 1-20 characters, alphanumeric
 * - floor: optional, max 10 characters
 * - status: required, must be AVAILABLE, OCCUPIED, or MAINTENANCE
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new room")
public class CreateRoomRequest {

    @Schema(description = "ID of the motel chain", example = "f22bdf78-4713-4292-ac50-a2be85155115", required = true)
    @NotNull(message = "Motel chain ID is required")
    private UUID motelChainId;

    @Schema(description = "ID of the motel", example = "a1b2c3d4-5678-90ab-cdef-123456789abc", required = true)
    @NotNull(message = "Motel ID is required")
    private UUID motelId;

    @Schema(description = "ID of the room category", example = "b57522e9-2ecb-4b5e-9480-9b3cad04d47b", required = true)
    @NotNull(message = "Room category ID is required")
    private UUID motelRoomCategoryId;

    @Schema(description = "Room number (1-20 characters, alphanumeric)", example = "101", required = true)
    @NotBlank(message = "Room number is required")
    @Size(min = 1, max = 20, message = "Room number must be between 1 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Room number must be alphanumeric (letters, numbers, and hyphens only)")
    private String roomNumber;

    @Schema(description = "Floor number or identifier", example = "1", required = false)
    @Size(max = 10, message = "Floor must not exceed 10 characters")
    private String floor;

    @Schema(description = "Room status", example = "AVAILABLE", allowableValues = {"AVAILABLE", "OCCUPIED", "MAINTENANCE"}, required = true)
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "AVAILABLE|OCCUPIED|MAINTENANCE", message = "Status must be AVAILABLE, OCCUPIED, or MAINTENANCE")
    private String status;
}
