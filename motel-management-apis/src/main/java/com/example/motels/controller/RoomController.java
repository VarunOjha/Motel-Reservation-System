package com.example.motels.controller;

import com.example.motels.dto.mapper.RoomMapper;
import com.example.motels.dto.request.CreateRoomRequest;
import com.example.motels.dto.request.UpdateRoomRequest;
import com.example.motels.dto.response.RoomResponse;
import com.example.motels.model.ApiResponse;
import com.example.motels.model.PaginatedResponse;
import com.example.motels.model.Room;
import com.example.motels.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/motelApi/v1/motelRooms")
@Tag(name = "Room Management", description = "APIs for managing hotel rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "roomNumber", "floor", "status", "createdAt", "updatedAt"
    );

    @GetMapping
    @Operation(summary = "Get all rooms with pagination and filtering", 
               description = "Retrieve paginated list of rooms with optional filters")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rooms retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<ApiResponse<PaginatedResponse<RoomResponse>>> getAllRooms(
            @RequestParam(value = "motelID", required = false) UUID motelId,
            @RequestParam(value = "motelChainID", required = false) UUID motelChainId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "roomNumber") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        // Validate sort field
        if (!ALLOWED_SORT_FIELDS.contains(sort)) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("400", null, "Invalid sort field. Allowed fields: " + ALLOWED_SORT_FIELDS)
            );
        }
        
        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        
        Page<Room> roomPage = roomService.getAllRoomsWithFilters(motelId, motelChainId, status, pageable);
        
        List<RoomResponse> responses = roomMapper.toResponseList(roomPage.getContent());
        PaginatedResponse.PaginationInfo paginationInfo = new PaginatedResponse.PaginationInfo(
            roomPage.getNumber(),
            roomPage.getSize(),
            roomPage.getTotalElements(),
            roomPage.getTotalPages(),
            roomPage.isFirst(),
            roomPage.isLast()
        );
        PaginatedResponse<RoomResponse> paginatedResponse = new PaginatedResponse<>(responses, paginationInfo);
        
        return ResponseEntity.ok(new ApiResponse<>("200", paginatedResponse, "Rooms retrieved successfully"));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Get room by ID", description = "Retrieve a specific room by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable UUID roomId) {
        Room room = roomService.getRoomById(roomId);
        RoomResponse response = roomMapper.toResponse(room);
        return ResponseEntity.ok(new ApiResponse<>("200", response, "Room retrieved successfully"));
    }

    @PostMapping
    @Operation(summary = "Create a new room", description = "Create a new room with validation")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Room created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Room already exists")
    })
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Room room = roomMapper.toEntity(request);
        Room createdRoom = roomService.createRoom(room);
        RoomResponse response = roomMapper.toResponse(createdRoom);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("201", response, "Room created successfully"));
    }

    @PutMapping("/{roomId}")
    @Operation(summary = "Update a room", description = "Update room details (partial update supported)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable UUID roomId,
            @Valid @RequestBody UpdateRoomRequest request) {
        Room existing = roomService.getRoomById(roomId);
        roomMapper.updateEntityFromDto(request, existing);
        Room updated = roomService.updateRoom(roomId, existing);
        RoomResponse response = roomMapper.toResponse(updated);
        return ResponseEntity.ok(new ApiResponse<>("200", response, "Room updated successfully"));
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "Delete a room", description = "Delete a room by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable UUID roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok(new ApiResponse<>("200", null, "Room deleted successfully"));
    }
}
