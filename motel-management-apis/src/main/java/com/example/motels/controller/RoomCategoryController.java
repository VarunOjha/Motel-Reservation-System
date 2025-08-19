package com.example.motels.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.motels.model.ApiResponse;
import com.example.motels.service.RoomCategoryService;
import com.example.motels.model.RoomCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/motelApi/v1/motelRoomCategories")
public class RoomCategoryController {

    @Autowired
    private RoomCategoryService roomCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllRoomCategories(
            @RequestParam(value = "motelID", required = false) String motelIdStr,
            @RequestParam(value = "motelChainID", required = false) String motelChainIdStr,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        // Validate pagination parameters
        if (page < 0) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("400", null, "Page number cannot be negative");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (size <= 0 || size > 100) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("400", null, "Page size must be between 1 and 100");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Convert string parameters to UUID if provided
        UUID motelId = null;
        UUID motelChainId = null;
        
        if (motelIdStr != null && !motelIdStr.trim().isEmpty()) {
            try {
                motelId = UUID.fromString(motelIdStr);
            } catch (IllegalArgumentException e) {
                ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("400", null, "Invalid motelID format");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        if (motelChainIdStr != null && !motelChainIdStr.trim().isEmpty()) {
            try {
                motelChainId = UUID.fromString(motelChainIdStr);
            } catch (IllegalArgumentException e) {
                ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>("400", null, "Invalid motelChainID format");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        // Create Pageable object
        Pageable pageable = PageRequest.of(page, size);
        
        // Get paginated results
        Page<RoomCategory> roomCategoriesPage = roomCategoryService.getAllRoomCategoriesWithFilters(motelId, motelChainId, status, pageable);
        
        // Create response with pagination info
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", roomCategoriesPage.getContent());
        responseData.put("pagination", Map.of(
            "currentPage", roomCategoriesPage.getNumber(),
            "totalPages", roomCategoriesPage.getTotalPages(),
            "totalElements", roomCategoriesPage.getTotalElements(),
            "pageSize", roomCategoriesPage.getSize(),
            "hasNext", roomCategoriesPage.hasNext(),
            "hasPrevious", roomCategoriesPage.hasPrevious()
        ));
        
        ApiResponse<Map<String, Object>> response = new ApiResponse<>("200", responseData);
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
