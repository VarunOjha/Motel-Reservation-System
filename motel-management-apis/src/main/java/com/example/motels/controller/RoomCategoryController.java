package com.example.motels.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.motels.model.ApiResponse;
import com.example.motels.service.RoomCategoryService;
import com.example.motels.model.RoomCategory;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/motelApi/v1/motelRoomCategories")
public class RoomCategoryController {

    @Autowired
    private RoomCategoryService roomCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomCategory>>> getAllRoomCategories(
            @RequestParam(value = "motelID", required = false) String motelIdStr,
            @RequestParam(value = "motelChainID", required = false) String motelChainIdStr,
            @RequestParam(value = "status", required = false) String status) {
        
        // Convert string parameters to UUID if provided
        UUID motelId = null;
        UUID motelChainId = null;
        
        if (motelIdStr != null && !motelIdStr.trim().isEmpty()) {
            try {
                motelId = UUID.fromString(motelIdStr);
            } catch (IllegalArgumentException e) {
                ApiResponse<List<RoomCategory>> errorResponse = new ApiResponse<>("400", null, "Invalid motelID format");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        if (motelChainIdStr != null && !motelChainIdStr.trim().isEmpty()) {
            try {
                motelChainId = UUID.fromString(motelChainIdStr);
            } catch (IllegalArgumentException e) {
                ApiResponse<List<RoomCategory>> errorResponse = new ApiResponse<>("400", null, "Invalid motelChainID format");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        List<RoomCategory> roomCategories = roomCategoryService.getAllRoomCategoriesWithFilters(motelId, motelChainId, status);
        ApiResponse<List<RoomCategory>> response = new ApiResponse<>("200", roomCategories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roomCategoryId}")
    public ResponseEntity<ApiResponse<RoomCategory>> getRoomCategoryById(@PathVariable UUID roomCategoryId) {
        return roomCategoryService.getRoomCategoryById(roomCategoryId)
                .map(roomCategory -> {
                    ApiResponse<RoomCategory> response = new ApiResponse<>("200", roomCategory);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    ApiResponse<RoomCategory> response = new ApiResponse<>("404", null, "Room category not found");
                    return ResponseEntity.status(404).body(response);
                });
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomCategory>> createRoomCategory(@RequestBody RoomCategory roomCategory) {
        // Validate required fields from payload
        if (roomCategory.getMotelChainId() == null) {
            ApiResponse<RoomCategory> errorResponse = new ApiResponse<>("400", null, "motelChainId is required in the payload");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (roomCategory.getMotelId() == null) {
            ApiResponse<RoomCategory> errorResponse = new ApiResponse<>("400", null, "motelId is required in the payload");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        RoomCategory createdRoomCategory = roomCategoryService.createRoomCategory(roomCategory);
        ApiResponse<RoomCategory> response = new ApiResponse<>("201", createdRoomCategory);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{roomCategoryId}")
    public ResponseEntity<ApiResponse<RoomCategory>> updateRoomCategory(@PathVariable UUID roomCategoryId, @RequestBody RoomCategory roomCategory) {
        // Validate required fields from payload
        if (roomCategory.getMotelChainId() == null) {
            ApiResponse<RoomCategory> errorResponse = new ApiResponse<>("400", null, "motelChainId is required in the payload");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (roomCategory.getMotelId() == null) {
            ApiResponse<RoomCategory> errorResponse = new ApiResponse<>("400", null, "motelId is required in the payload");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        RoomCategory updatedRoomCategory = roomCategoryService.updateRoomCategory(roomCategoryId, roomCategory);
        if (updatedRoomCategory != null) {
            ApiResponse<RoomCategory> response = new ApiResponse<>("200", updatedRoomCategory);
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<RoomCategory> response = new ApiResponse<>("404", null, "Room category not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    @DeleteMapping("/{roomCategoryId}")
    public ResponseEntity<ApiResponse<String>> deleteRoomCategory(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @PathVariable UUID roomCategoryId) {
        if (roomCategoryService.deleteRoomCategory(motelChainId, motelId, roomCategoryId)) {
            ApiResponse<String> response = new ApiResponse<>("204", "Room category deleted successfully");
            return ResponseEntity.status(204).body(response);
        } else {
            ApiResponse<String> response = new ApiResponse<>("404", null, "Room category not found");
            return ResponseEntity.status(404).body(response);
        }
    }
}
