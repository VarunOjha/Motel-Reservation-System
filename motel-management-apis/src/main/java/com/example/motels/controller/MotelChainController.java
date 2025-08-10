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
import com.example.motels.service.MotelChainService;
import com.example.motels.model.MotelChain; // Ensure this path matches the actual location of the MotelChain class
import java.util.UUID;

@RestController
@RequestMapping("/motelApi/v1/motelChains")
public class MotelChainController {

    @Autowired
    private MotelChainService motelChainService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<MotelChain>>> getAllMotelChains(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String sortDirection) {
        
        // Validate page and size parameters
        if (page < 0) {
            ApiResponse<PaginatedResponse<MotelChain>> errorResponse = new ApiResponse<>("400", null, "Page number cannot be negative");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (size <= 0 || size > 100) {
            ApiResponse<PaginatedResponse<MotelChain>> errorResponse = new ApiResponse<>("400", null, "Page size must be between 1 and 100");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Create sort object
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get paginated results
        Page<MotelChain> motelChainPage = motelChainService.getAllMotelChains(pageable);
        
        // Create pagination info
        PaginatedResponse.PaginationInfo paginationInfo = new PaginatedResponse.PaginationInfo(
            motelChainPage.getNumber(),
            motelChainPage.getSize(),
            motelChainPage.getTotalElements(),
            motelChainPage.getTotalPages(),
            motelChainPage.isFirst(),
            motelChainPage.isLast()
        );
        
        // Create paginated response
        PaginatedResponse<MotelChain> paginatedResponse = new PaginatedResponse<>(
            motelChainPage.getContent(),
            paginationInfo
        );
        
        ApiResponse<PaginatedResponse<MotelChain>> response = new ApiResponse<>("200", paginatedResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MotelChain>> getMotelChainById(@PathVariable UUID id) {
        return motelChainService.getMotelChainById(id)
                .map(motelChain -> {
                    ApiResponse<MotelChain> response = new ApiResponse<>("200", motelChain);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    ApiResponse<MotelChain> response = new ApiResponse<>("404", null);
                    return ResponseEntity.status(404).body(response);
                });
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MotelChain>> createMotelChain(@RequestBody MotelChain motelChain) {
        // Validate required fields
        if (motelChain.getMotelChainName() == null || motelChain.getMotelChainName().trim().isEmpty()) {
            ApiResponse<MotelChain> errorResponse = new ApiResponse<>("400", null, "motelChainName is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (motelChain.getState() == null || motelChain.getState().trim().isEmpty()) {
            ApiResponse<MotelChain> errorResponse = new ApiResponse<>("400", null, "state is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (motelChain.getPincode() == null || motelChain.getPincode().trim().isEmpty()) {
            ApiResponse<MotelChain> errorResponse = new ApiResponse<>("400", null, "pincode is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (motelChain.getStatus() == null || motelChain.getStatus().trim().isEmpty()) {
            ApiResponse<MotelChain> errorResponse = new ApiResponse<>("400", null, "status is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        MotelChain createdMotelChain = motelChainService.createMotelChain(motelChain);
        ApiResponse<MotelChain> response = new ApiResponse<>("201", createdMotelChain);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MotelChain>> updateMotelChain(@PathVariable UUID id, @RequestBody MotelChain motelChain) {
        // Validate required fields
        if (motelChain.getMotelChainName() == null || motelChain.getMotelChainName().trim().isEmpty()) {
            ApiResponse<MotelChain> errorResponse = new ApiResponse<>("400", null, "motelChainName is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (motelChain.getState() == null || motelChain.getState().trim().isEmpty()) {
            ApiResponse<MotelChain> errorResponse = new ApiResponse<>("400", null, "state is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (motelChain.getPincode() == null || motelChain.getPincode().trim().isEmpty()) {
            ApiResponse<MotelChain> errorResponse = new ApiResponse<>("400", null, "pincode is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (motelChain.getStatus() == null || motelChain.getStatus().trim().isEmpty()) {
            ApiResponse<MotelChain> errorResponse = new ApiResponse<>("400", null, "status is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        MotelChain updatedMotelChain = motelChainService.updateMotelChain(id, motelChain);
        if (updatedMotelChain != null) {
            ApiResponse<MotelChain> response = new ApiResponse<>("200", updatedMotelChain);
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<MotelChain> response = new ApiResponse<>("404", null, "Motel chain not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMotelChain(@PathVariable UUID id) {
        if (motelChainService.deleteMotelChain(id)) {
            ApiResponse<String> response = new ApiResponse<>("204", "Motel chain deleted successfully");
            return ResponseEntity.status(204).body(response);
        } else {
            ApiResponse<String> response = new ApiResponse<>("404", "Motel chain not found");
            return ResponseEntity.status(404).body(response);
        }
    }
}