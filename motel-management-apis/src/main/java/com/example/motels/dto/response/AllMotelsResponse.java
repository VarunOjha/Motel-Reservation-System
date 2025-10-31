package com.example.motels.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for AllMotels API.
 * Provides complete motel information for listing purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing complete motel data")
public class AllMotelsResponse {

    @Schema(description = "Unique motel identifier", example = "a1b2c3d4-5678-90ab-cdef-123456789abc")
    private UUID motelId;

    @Schema(description = "ID of the motel chain", example = "f22bdf78-4713-4292-ac50-a2be85155115")
    private String motelChainId;

    @Schema(description = "Name of the motel", example = "Grand Plaza Hotel")
    private String motelName;

    @Schema(description = "Status of the motel", example = "ACTIVE")
    private String status;

    @Schema(description = "Postal/ZIP code", example = "560001")
    private String pincode;

    @Schema(description = "State or province", example = "Karnataka")
    private String state;

    @Schema(description = "Creation timestamp", example = "2025-10-31T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-10-31T09:00:00")
    private LocalDateTime updatedAt;
}
