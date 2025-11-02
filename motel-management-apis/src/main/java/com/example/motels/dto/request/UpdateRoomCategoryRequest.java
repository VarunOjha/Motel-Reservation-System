package com.example.motels.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing room category.
 * 
 * All fields are optional - only provided fields will be updated.
 * 
 * Validation Rules:
 * - roomCategoryName: if provided, 2-50 characters
 * - displayName: if provided, max 100 characters
 * - description: if provided, max 500 characters
 * - status: if provided, must be ACTIVE or INACTIVE
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating a room category. All fields are optional - only send fields you want to update.")
public class UpdateRoomCategoryRequest {
    
    @Schema(description = "Category name (2-50 characters)", example = "Premium Deluxe Suite", required = false)
    @Size(min = 2, max = 50, message = "Room category name must be between 2 and 50 characters")
    private String roomCategoryName;
    
    @Schema(description = "Display name for UI (max 100 characters)", example = "Premium Deluxe Suite - Ocean View", required = false)
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;
    
    @Schema(description = "Detailed description (max 500 characters)", example = "Updated luxury room with premium amenities", required = false)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Schema(description = "Status of the category", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, required = false)
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be either ACTIVE or INACTIVE")
    private String status;
}
