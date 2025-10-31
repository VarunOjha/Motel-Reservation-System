package com.example.motels.controller;

import com.example.motels.dto.mapper.MotelMapper;
import com.example.motels.dto.request.CreateMotelRequest;
import com.example.motels.dto.request.UpdateMotelRequest;
import com.example.motels.dto.response.MotelResponse;
import com.example.motels.model.ApiResponse;
import com.example.motels.model.Motel;
import com.example.motels.model.PaginatedResponse;
import com.example.motels.service.MotelService;
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
@RequestMapping("/motelApi/v1/motels")
@Tag(name = "Motel Management", description = "APIs for managing motels")
@RequiredArgsConstructor
public class MotelController {

    private final MotelService motelService;
    private final MotelMapper motelMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "motelName", "status", "pincode", "state", "createdAt", "updatedAt"
    );

    @GetMapping
    @Operation(summary = "Get all motels with pagination and filtering", 
               description = "Retrieve paginated list of motels with optional filters")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Motels retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<ApiResponse<PaginatedResponse<MotelResponse>>> getAllMotels(
            @RequestParam(value = "motelID", required = false) String motelIdStr,
            @RequestParam(value = "motelChainID", required = false) String motelChainId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pincode", required = false) String pincode,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(defaultValue = "motelName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        // Validate sort field
        if (!ALLOWED_SORT_FIELDS.contains(sort)) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("400", null, "Invalid sort field. Allowed fields: " + ALLOWED_SORT_FIELDS)
            );
        }
        
        // Convert motelID string to UUID if provided
        UUID motelId = null;
        if (motelIdStr != null && !motelIdStr.trim().isEmpty()) {
            try {
                motelId = UUID.fromString(motelIdStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>("400", null, "Invalid motelID format")
                );
            }
        }
        
        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        
        Page<Motel> motelPage = motelService.getMotelsWithFilters(motelId, motelChainId, status, pincode, state, pageable);
        
        List<MotelResponse> responses = motelMapper.toResponseList(motelPage.getContent());
        PaginatedResponse.PaginationInfo paginationInfo = new PaginatedResponse.PaginationInfo(
            motelPage.getNumber(),
            motelPage.getSize(),
            motelPage.getTotalElements(),
            motelPage.getTotalPages(),
            motelPage.isFirst(),
            motelPage.isLast()
        );
        PaginatedResponse<MotelResponse> paginatedResponse = new PaginatedResponse<>(responses, paginationInfo);
        
        return ResponseEntity.ok(new ApiResponse<>("200", paginatedResponse, "Motels retrieved successfully"));
    }

    @GetMapping("/{motelId}")
    @Operation(summary = "Get motel by ID", description = "Retrieve a specific motel by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Motel found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Motel not found")
    })
    public ResponseEntity<ApiResponse<MotelResponse>> getMotelById(@PathVariable UUID motelId) {
        Motel motel = motelService.getMotelById(motelId);
        MotelResponse response = motelMapper.toResponse(motel);
        return ResponseEntity.ok(new ApiResponse<>("200", response, "Motel retrieved successfully"));
    }

    @PostMapping
    @Operation(summary = "Create a new motel", description = "Create a new motel with validation")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Motel created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Motel already exists")
    })
    public ResponseEntity<ApiResponse<MotelResponse>> createMotel(@Valid @RequestBody CreateMotelRequest request) {
        Motel motel = motelMapper.toEntity(request);
        Motel createdMotel = motelService.createMotel(motel);
        MotelResponse response = motelMapper.toResponse(createdMotel);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("201", response, "Motel created successfully"));
    }

    @PutMapping("/{motelId}")
    @Operation(summary = "Update a motel", description = "Update motel details (partial update supported)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Motel updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Motel not found")
    })
    public ResponseEntity<ApiResponse<MotelResponse>> updateMotel(
            @PathVariable UUID motelId,
            @Valid @RequestBody UpdateMotelRequest request) {
        Motel existing = motelService.getMotelById(motelId);
        motelMapper.updateEntityFromDto(request, existing);
        Motel updated = motelService.updateMotel(existing);
        MotelResponse response = motelMapper.toResponse(updated);
        return ResponseEntity.ok(new ApiResponse<>("200", response, "Motel updated successfully"));
    }

    @DeleteMapping("/{motelId}")
    @Operation(summary = "Delete a motel", description = "Delete a motel by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Motel deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Motel not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteMotel(@PathVariable UUID motelId) {
        motelService.deleteMotel(motelId);
        return ResponseEntity.ok(new ApiResponse<>("200", null, "Motel deleted successfully"));
    }
}
