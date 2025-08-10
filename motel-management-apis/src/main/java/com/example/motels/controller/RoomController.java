package com.example.motels.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.motels.model.ApiResponse;
import com.example.motels.model.PaginatedResponse;
import com.example.motels.service.RoomService;
import com.example.motels.model.Room;

import java.util.UUID;

@RestController
@RequestMapping("/motelApi/v1/motelRooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<Room>>> getAllRooms(
            @RequestParam(value = "motelID", required = false) String motelIdStr,
            @RequestParam(value = "motelChainID", required = false) String motelChainIdStr,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String sortDirection) {
        
        // Validate pagination parameters
        if (page < 0) {
            ApiResponse<PaginatedResponse<Room>> errorResponse = new ApiResponse<>("400", null, "Page number cannot be negative");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (size <= 0 || size > 100) {
            ApiResponse<PaginatedResponse<Room>> errorResponse = new ApiResponse<>("400", null, "Page size must be between 1 and 100");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Convert string parameters to UUID if provided
        UUID motelId = null;
        UUID motelChainId = null;
        
        if (motelIdStr != null && !motelIdStr.trim().isEmpty()) {
            try {
                motelId = UUID.fromString(motelIdStr);
            } catch (IllegalArgumentException e) {
                ApiResponse<PaginatedResponse<Room>> errorResponse = new ApiResponse<>("400", null, "Invalid motelID format");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        if (motelChainIdStr != null && !motelChainIdStr.trim().isEmpty()) {
            try {
                motelChainId = UUID.fromString(motelChainIdStr);
            } catch (IllegalArgumentException e) {
                ApiResponse<PaginatedResponse<Room>> errorResponse = new ApiResponse<>("400", null, "Invalid motelChainID format");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        // Create sort object
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Check if we need to apply filters
        boolean hasFilters = motelId != null || motelChainId != null || status != null;
        
        Page<Room> roomPage;
        if (hasFilters) {
            // For filtered results, use the paginated filter method
            roomPage = roomService.getAllRoomsWithFilters(motelId, motelChainId, status, pageable);
        } else {
            // No filters, return paginated results
            roomPage = roomService.getAllRooms(pageable);
        }
        
        // Create pagination info
        PaginatedResponse.PaginationInfo paginationInfo = new PaginatedResponse.PaginationInfo(
            roomPage.getNumber(),
            roomPage.getSize(),
            roomPage.getTotalElements(),
            roomPage.getTotalPages(),
            roomPage.isFirst(),
            roomPage.isLast()
        );
        
        // Create paginated response
        PaginatedResponse<Room> paginatedResponse = new PaginatedResponse<>(
            roomPage.getContent(),
            paginationInfo
        );
        
        ApiResponse<PaginatedResponse<Room>> response = new ApiResponse<>("200", paginatedResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Room>> getRoomById(@PathVariable UUID roomId) {
        return roomService.getRoomById(roomId)
                .map(room -> {
                    ApiResponse<Room> response = new ApiResponse<>("200", room);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    ApiResponse<Room> response = new ApiResponse<>("404", null, "Room not found");
                    return ResponseEntity.status(404).body(response);
                });
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Room>> createRoom(@RequestBody Room room) {
        // Validate required fields from payload
        if (room.getMotelChainId() == null) {
            ApiResponse<Room> errorResponse = new ApiResponse<>("400", null, "motelChainId is required in the payload");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (room.getMotelId() == null) {
            ApiResponse<Room> errorResponse = new ApiResponse<>("400", null, "motelId is required in the payload");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (room.getStatus() == null || room.getStatus().trim().isEmpty()) {
            ApiResponse<Room> errorResponse = new ApiResponse<>("400", null, "status is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        Room createdRoom = roomService.createRoom(room);
        ApiResponse<Room> response = new ApiResponse<>("201", createdRoom);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Room>> updateRoom(@PathVariable UUID roomId, @RequestBody Room room) {
        // Validate required fields from payload
        if (room.getMotelChainId() == null) {
            ApiResponse<Room> errorResponse = new ApiResponse<>("400", null, "motelChainId is required in the payload");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (room.getMotelId() == null) {
            ApiResponse<Room> errorResponse = new ApiResponse<>("400", null, "motelId is required in the payload");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (room.getStatus() == null || room.getStatus().trim().isEmpty()) {
            ApiResponse<Room> errorResponse = new ApiResponse<>("400", null, "status is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        Room updatedRoom = roomService.updateRoom(roomId, room);
        if (updatedRoom != null) {
            ApiResponse<Room> response = new ApiResponse<>("200", updatedRoom);
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Room> response = new ApiResponse<>("404", null, "Room not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<String>> deleteRoom(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @PathVariable UUID roomId) {
        if (roomService.deleteRoom(motelChainId, motelId, roomId)) {
            ApiResponse<String> response = new ApiResponse<>("204", "Room deleted successfully");
            return ResponseEntity.status(204).body(response);
        } else {
            ApiResponse<String> response = new ApiResponse<>("404", null, "Room not found");
            return ResponseEntity.status(404).body(response);
        }
    }
}
