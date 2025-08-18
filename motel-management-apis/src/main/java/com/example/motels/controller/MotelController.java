package com.example.motels.controller;

import com.example.motels.model.ApiResponse;
import com.example.motels.model.Motel;
import com.example.motels.model.PaginatedResponse;
import com.example.motels.service.MotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/motelApi/v1/motels")
public class MotelController {

    @Autowired
    private MotelService motelService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<Motel>>> getAllMotels(
            @RequestParam(value = "motelID", required = false) String motelIdStr,
            @RequestParam(value = "motelChainID", required = false) String motelChainId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pincode", required = false) String pincode,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String sortDirection) {
        
        // Validate pagination parameters
        if (page < 0) {
            ApiResponse<PaginatedResponse<Motel>> errorResponse = new ApiResponse<>("400", null, "Page number cannot be negative");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (size <= 0 || size > 100) {
            ApiResponse<PaginatedResponse<Motel>> errorResponse = new ApiResponse<>("400", null, "Page size must be between 1 and 100");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Convert motelID string to UUID if provided
        UUID motelId = null;
        if (motelIdStr != null && !motelIdStr.trim().isEmpty()) {
            try {
                motelId = UUID.fromString(motelIdStr);
            } catch (IllegalArgumentException e) {
                // If UUID is invalid, return bad request
                ApiResponse<PaginatedResponse<Motel>> errorResponse = new ApiResponse<>("400", null, "Invalid motelID format");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        // Create sort object
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Check if we need to apply filters
        boolean hasFilters = motelId != null || motelChainId != null || status != null || pincode != null || state != null;
        
        Page<Motel> motelPage;
        if (hasFilters) {
            // For filtered results, use the paginated filter method
            // Note: This currently returns paginated results without applying filters to the pagination
            // In production, you'd want to implement proper filtered pagination in the repository
            motelPage = motelService.getMotelsWithFilters(motelId, motelChainId, status, pincode, state, pageable);
        } else {
            // No filters, return paginated results
            motelPage = motelService.getAllMotels(pageable);
        }
        
        // Create pagination info
        PaginatedResponse.PaginationInfo paginationInfo = new PaginatedResponse.PaginationInfo(
            motelPage.getNumber(),
            motelPage.getSize(),
            motelPage.getTotalElements(),
            motelPage.getTotalPages(),
            motelPage.isFirst(),
            motelPage.isLast()
        );
        
        // Create paginated response
        PaginatedResponse<Motel> paginatedResponse = new PaginatedResponse<>(
            motelPage.getContent(),
            paginationInfo
        );
        
        ApiResponse<PaginatedResponse<Motel>> response = new ApiResponse<>("200", paginatedResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{motelId}")
    public ResponseEntity<ApiResponse<Motel>> getMotelById(@PathVariable UUID motelId) {
        Optional<Motel> motel = motelService.getMotelById(motelId);
        if (motel.isPresent()) {
            ApiResponse<Motel> response = new ApiResponse<>("200", motel.get());
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Motel> response = new ApiResponse<>("404", null);
            return ResponseEntity.status(404).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Motel>> createMotel(@RequestBody Motel motel) {
        // Validate that required fields are provided in the payload
        if (motel.getMotelChainId() == null || motel.getMotelChainId().trim().isEmpty()) {
            ApiResponse<Motel> errorResponse = new ApiResponse<>("400", null, "motelChainId is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (motel.getMotelName() == null || motel.getMotelName().trim().isEmpty()) {
            ApiResponse<Motel> errorResponse = new ApiResponse<>("400", null, "motelName is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (motel.getPincode() == null || motel.getPincode().trim().isEmpty()) {
            ApiResponse<Motel> errorResponse = new ApiResponse<>("400", null, "pincode is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (motel.getState() == null || motel.getState().trim().isEmpty()) {
            ApiResponse<Motel> errorResponse = new ApiResponse<>("400", null, "state is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Check if motel already exists with the same motelChainId, motelName, pincode, and state
        Optional<Motel> existingMotel = motelService.findByMotelChainIdAndMotelNameAndPincodeAndState(
            motel.getMotelChainId(), motel.getMotelName(), motel.getPincode(), motel.getState());
        
        if (existingMotel.isPresent()) {
            // Return existing motel with 201 code
            ApiResponse<Motel> response = new ApiResponse<>("201", existingMotel.get(), "Motel already exists");
            return ResponseEntity.status(201).body(response);
        }
        
        Motel createdMotel = motelService.createMotel(motel);
        ApiResponse<Motel> response = new ApiResponse<>("201", createdMotel, "Motel created successfully");
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{motelId}")
    public ResponseEntity<ApiResponse<Motel>> updateMotel(@PathVariable UUID motelId, @RequestBody Motel motel) {
        // Validate that motelChainId is provided in the payload
        if (motel.getMotelChainId() == null || motel.getMotelChainId().trim().isEmpty()) {
            ApiResponse<Motel> errorResponse = new ApiResponse<>("400", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        motel.setMotelId(motelId);
        Motel updatedMotel = motelService.updateMotel(motel);
        ApiResponse<Motel> response = new ApiResponse<>("200", updatedMotel);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{motelId}")
    public ResponseEntity<ApiResponse<String>> deleteMotel(@PathVariable UUID motelId) {
        motelService.deleteMotel(motelId);
        ApiResponse<String> response = new ApiResponse<>("204", "Motel deleted successfully");
        return ResponseEntity.status(204).body(response);
    }
}
