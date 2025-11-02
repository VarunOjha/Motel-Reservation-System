package com.example.motels.controller;

import com.example.motels.dto.mapper.RoomCategoryMapper;
import com.example.motels.dto.request.CreateRoomCategoryRequest;
import com.example.motels.dto.request.UpdateRoomCategoryRequest;
import com.example.motels.dto.response.RoomCategoryResponse;
import com.example.motels.model.ApiResponse;
import com.example.motels.model.PaginatedResponse;
import com.example.motels.model.RoomCategory;
import com.example.motels.service.RoomCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/motelApi/v1/motelRoomCategories")
@Tag(name = "Room Category Management", description = "APIs for managing room categories, pricing tiers, and room types")
@Slf4j
public class RoomCategoryController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "roomCategoryName", "displayName", "status", "createdAt", "updatedAt"
    );

    @Autowired
    private RoomCategoryService roomCategoryService;
    
    @Autowired
    private RoomCategoryMapper roomCategoryMapper;

    @GetMapping
    @Operation(
        summary = "Get all room categories with pagination and filtering",
        description = "Retrieves paginated list of room categories with optional filters for motel, chain, and status"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room categories retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<ApiResponse<PaginatedResponse<RoomCategoryResponse>>> getAllRoomCategories(
            @RequestParam(value = "motelID", required = false) UUID motelId,
            @RequestParam(value = "motelChainID", required = false) UUID motelChainId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "roomCategoryName") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String sortDirection) {
        
        // Validate pagination parameters
        if (page < 0 || size <= 0 || size > 100) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("400", null, "Invalid pagination parameters")
            );
        }
        
        // Validate sort field
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("400", null, 
                    "Invalid sort field. Allowed fields: " + ALLOWED_SORT_FIELDS)
            );
        }
        
        // Create Pageable with sorting
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Get paginated results
        Page<RoomCategory> entityPage = roomCategoryService.getAllRoomCategoriesWithFilters(
            motelId, motelChainId, status, pageable);
        
        // Convert to DTOs
        List<RoomCategoryResponse> responses = roomCategoryMapper.toResponseList(entityPage.getContent());
        
        // Create pagination info
        PaginatedResponse.PaginationInfo paginationInfo = new PaginatedResponse.PaginationInfo(
            entityPage.getNumber(),
            entityPage.getSize(),
            entityPage.getTotalElements(),
            entityPage.getTotalPages(),
            entityPage.isFirst(),
            entityPage.isLast()
        );
        
        // Create paginated response
        PaginatedResponse<RoomCategoryResponse> paginatedResponse = new PaginatedResponse<>(
            responses,
            paginationInfo
        );
        
        return ResponseEntity.ok(
            new ApiResponse<>("200", paginatedResponse, "Room categories retrieved successfully")
        );
    }

    @GetMapping("/{roomCategoryId}")
    @Operation(
        summary = "Get room category by ID",
        description = "Retrieves a specific room category by its unique identifier"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room category found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room category not found")
    })
    public ResponseEntity<ApiResponse<RoomCategoryResponse>> getRoomCategoryById(
            @PathVariable UUID roomCategoryId) {
        
        RoomCategory entity = roomCategoryService.getRoomCategoryById(roomCategoryId);
        RoomCategoryResponse response = roomCategoryMapper.toResponse(entity);
        
        return ResponseEntity.ok(
            new ApiResponse<>("200", response, "Room category retrieved successfully")
        );
    }

    @PostMapping
    @Operation(
        summary = "Create new room category",
        description = "Creates a new room category with validation. Category name must be unique within the motel."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Room category created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Room category already exists")
    })
    public ResponseEntity<ApiResponse<RoomCategoryResponse>> createRoomCategory(
            @Valid @RequestBody CreateRoomCategoryRequest request) {
        
        // Convert DTO to entity
        RoomCategory entity = roomCategoryMapper.toEntity(request);
        
        // Save entity
        RoomCategory saved = roomCategoryService.createRoomCategory(entity);
        
        // Convert to response DTO
        RoomCategoryResponse response = roomCategoryMapper.toResponse(saved);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new ApiResponse<>("201", response, "Room category created successfully"));
    }

    @PutMapping("/{roomCategoryId}")
    @Operation(
        summary = "Update room category",
        description = "Updates an existing room category. Only provided fields will be updated."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room category updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room category not found")
    })
    public ResponseEntity<ApiResponse<RoomCategoryResponse>> updateRoomCategory(
            @PathVariable UUID roomCategoryId,
            @Valid @RequestBody UpdateRoomCategoryRequest request) {
        
        // Get existing entity
        RoomCategory existing = roomCategoryService.getRoomCategoryById(roomCategoryId);
        
        // Update entity from request (only non-null fields)
        roomCategoryMapper.updateEntityFromRequest(request, existing);
        
        // Save updated entity
        RoomCategory updated = roomCategoryService.updateRoomCategory(roomCategoryId, existing);
        
        // Convert to response DTO
        RoomCategoryResponse response = roomCategoryMapper.toResponse(updated);
        
        return ResponseEntity.ok(
            new ApiResponse<>("200", response, "Room category updated successfully")
        );
    }

    @DeleteMapping("/{roomCategoryId}")
    @Operation(
        summary = "Delete room category",
        description = "Deletes a room category by its ID"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Room category deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Room category not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteRoomCategory(
            @PathVariable UUID roomCategoryId) {
        
        roomCategoryService.deleteRoomCategory(roomCategoryId);
        
        return ResponseEntity.ok(
            new ApiResponse<>("200", null, "Room category deleted successfully")
        );
    }
}
