package com.example.motels.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Room entity.
 * Contains all room information without internal/audit fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing room data")
public class RoomResponse {

    @Schema(description = "Unique room identifier", example = "c1d2e3f4-5678-90ab-cdef-123456789abc")
    private UUID roomId;

    @Schema(description = "ID of the motel chain", example = "f22bdf78-4713-4292-ac50-a2be85155115")
    private UUID motelChainId;

    @Schema(description = "ID of the motel", example = "a1b2c3d4-5678-90ab-cdef-123456789abc")
    private UUID motelId;

    @Schema(description = "ID of the room category", example = "b57522e9-2ecb-4b5e-9480-9b3cad04d47b")
    private UUID motelRoomCategoryId;

    @Schema(description = "Room number", example = "101")
    private String roomNumber;

    @Schema(description = "Floor number or identifier", example = "1")
    private String floor;

    @Schema(description = "Room status", example = "AVAILABLE")
    private String status;

    @Schema(description = "Creation timestamp", example = "2025-10-31T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-10-31T09:00:00")
    private LocalDateTime updatedAt;
}
