package com.example.motels.controller;

import com.example.motels.dto.mapper.MotelChainMapper;
import com.example.motels.dto.request.CreateMotelChainRequest;
import com.example.motels.dto.request.UpdateMotelChainRequest;
import com.example.motels.dto.response.MotelChainResponse;
import com.example.motels.model.ApiResponse;
import com.example.motels.model.PaginatedResponse;
import com.example.motels.model.MotelChain;
import com.example.motels.service.MotelChainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for MotelChain API.
 * 
 * Key improvements:
 * - Uses DTOs for request/response (no entity exposure)
 * - Bean Validation with @Valid (no manual validation)
 * - Exceptions handled by GlobalExceptionHandler
 * - Clean, focused controller logic
 * - Swagger/OpenAPI documentation
 */
@Tag(name = "Motel Chain Management", description = "APIs for managing motel chains including CRUD operations, pagination, and search")
@RestController
@RequestMapping("/motelApi/v1/motelChains")
public class MotelChainController {

    private final MotelChainService motelChainService;
    private final MotelChainMapper mapper;

    /**
     * Constructor injection (preferred over @Autowired field injection).
     * Enables immutability and easier testing.
     */
    public MotelChainController(MotelChainService motelChainService, MotelChainMapper mapper) {
        this.motelChainService = motelChainService;
        this.mapper = mapper;
    }

    /**
     * GET /motelApi/v1/motelChains
     * List all motel chains with pagination and sorting.
     * 
     * Returns MotelChainResponse DTOs (not entities).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<MotelChainResponse>>> getAllMotelChains(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String sortDirection) {
        
        // Basic validation (could be extracted to a validator class)
        if (page < 0) {
            ApiResponse<PaginatedResponse<MotelChainResponse>> errorResponse = 
                new ApiResponse<>("400", null, "Page number cannot be negative");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (size <= 0 || size > 100) {
            ApiResponse<PaginatedResponse<MotelChainResponse>> errorResponse = 
                new ApiResponse<>("400", null, "Page size must be between 1 and 100");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Create sort and pageable objects
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Fetch entities from service
        Page<MotelChain> entityPage = motelChainService.getAllMotelChains(pageable);
        
        // Convert entities to response DTOs
        List<MotelChainResponse> responseDtos = mapper.toResponseList(entityPage.getContent());
        
        // Create pagination metadata
        PaginatedResponse.PaginationInfo paginationInfo = new PaginatedResponse.PaginationInfo(
            entityPage.getNumber(),
            entityPage.getSize(),
            entityPage.getTotalElements(),
            entityPage.getTotalPages(),
            entityPage.isFirst(),
            entityPage.isLast()
        );
        
        // Wrap in response format
        PaginatedResponse<MotelChainResponse> paginatedResponse = 
            new PaginatedResponse<>(responseDtos, paginationInfo);
        
        ApiResponse<PaginatedResponse<MotelChainResponse>> response = 
            new ApiResponse<>("200", paginatedResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /motelApi/v1/motelChains/{id}
     * Get a single motel chain by ID.
     * 
     * Throws ResourceNotFoundException if not found (handled by GlobalExceptionHandler).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MotelChainResponse>> getMotelChainById(@PathVariable UUID id) {
        // Service throws exception if not found, no need for Optional handling
        MotelChain entity = motelChainService.getMotelChainById(id);
        
        // Convert entity to response DTO
        MotelChainResponse responseDto = mapper.toResponse(entity);
        
        ApiResponse<MotelChainResponse> response = new ApiResponse<>("200", responseDto);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /motelApi/v1/motelChains
     * Create a new motel chain.
     * 
     * @Valid triggers Bean Validation (validates @NotBlank, @Size, etc.)
     * Validation errors handled by GlobalExceptionHandler.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MotelChainResponse>> createMotelChain(
            @Valid @RequestBody CreateMotelChainRequest request) {
        
        // No manual validation needed! @Valid + GlobalExceptionHandler handles it.
        
        // Service handles business logic (duplicate check, etc.)
        MotelChain createdEntity = motelChainService.createMotelChain(request);
        
        // Convert entity to response DTO
        MotelChainResponse responseDto = mapper.toResponse(createdEntity);
        
        ApiResponse<MotelChainResponse> response = new ApiResponse<>("201", responseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /motelApi/v1/motelChains/{id}
     * Update an existing motel chain.
     * 
     * @Valid validates the request body.
     * Service throws ResourceNotFoundException if ID doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MotelChainResponse>> updateMotelChain(
            @PathVariable UUID id, 
            @Valid @RequestBody UpdateMotelChainRequest request) {
        
        // Service handles update logic and exception throwing
        MotelChain updatedEntity = motelChainService.updateMotelChain(id, request);
        
        // Convert entity to response DTO
        MotelChainResponse responseDto = mapper.toResponse(updatedEntity);
        
        ApiResponse<MotelChainResponse> response = new ApiResponse<>("200", responseDto);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /motelApi/v1/motelChains/{id}
     * Delete (soft delete) a motel chain.
     * 
     * Service throws ResourceNotFoundException if ID doesn't exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMotelChain(@PathVariable UUID id) {
        motelChainService.deleteMotelChain(id);
        
        // HTTP 204 No Content is standard for successful DELETE
        ApiResponse<Void> response = new ApiResponse<>("204", null, "Motel chain deleted successfully");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}