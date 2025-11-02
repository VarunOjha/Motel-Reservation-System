package com.example.motels.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for room category data.
 * 
 * Contains all room category information to be returned to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomCategoryResponse {
    
    private UUID motelRoomCategoryId;
    private UUID motelChainId;
    private UUID motelId;
    private String roomCategoryName;
    private String displayName;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
