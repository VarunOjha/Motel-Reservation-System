package com.example.motels.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new room category.
 * 
 * Validation Rules:
 * - motelChainId: required
 * - motelId: required
 * - roomCategoryName: required, 2-50 characters
 * - displayName: optional, max 100 characters
 * - description: optional, max 500 characters
 * - status: required, must be ACTIVE or INACTIVE
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new room category")
public class CreateRoomCategoryRequest {
    
    @Schema(description = "ID of the motel chain this category belongs to", example = "f22bdf78-4713-4292-ac50-a2be85155115", required = true)
    @NotNull(message = "Motel chain ID is required")
    private UUID motelChainId;
    
    @Schema(description = "ID of the motel this category belongs to", example = "a1b2c3d4-5678-90ab-cdef-123456789abc", required = true)
    @NotNull(message = "Motel ID is required")
    private UUID motelId;
    
    @Schema(description = "Name of the room category (2-50 characters)", example = "Deluxe Suite", required = true)
    @NotBlank(message = "Room category name is required")
    @Size(min = 2, max = 50, message = "Room category name must be between 2 and 50 characters")
    private String roomCategoryName;
    
    @Schema(description = "Display name for UI (max 100 characters)", example = "Deluxe Suite - Ocean View", required = false)
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;
    
    @Schema(description = "Detailed description (max 500 characters)", example = "Luxury room with stunning ocean views, king-size bed, and premium amenities", required = false)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Schema(description = "Status of the category", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, required = true)
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be either ACTIVE or INACTIVE")
    private String status;
}
