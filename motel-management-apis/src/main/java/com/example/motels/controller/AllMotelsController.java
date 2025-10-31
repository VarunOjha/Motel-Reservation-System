package com.example.motels.controller;

import com.example.motels.dto.mapper.AllMotelsMapper;
import com.example.motels.dto.response.AllMotelsCountResponse;
import com.example.motels.dto.response.AllMotelsResponse;
import com.example.motels.model.ApiResponse;
import com.example.motels.model.Motel;
import com.example.motels.model.PaginatedResponse;
import com.example.motels.repository.MotelChainRepository;
import com.example.motels.repository.MotelRepository;
import com.example.motels.repository.RoomCategoryRepository;
import com.example.motels.repository.RoomRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/motelApi/v1/allMotels")
@Tag(name = "All Motels", description = "Read-only aggregate API for motel data and statistics")
@RequiredArgsConstructor
public class AllMotelsController {

    private final MotelRepository motelRepository;
    private final MotelChainRepository motelChainRepository;
    private final RoomRepository roomRepository;
    private final RoomCategoryRepository roomCategoryRepository;
    private final AllMotelsMapper allMotelsMapper;

    @GetMapping
    @Operation(summary = "Get all motels with pagination",
               description = "Retrieve paginated list of all motels in the system")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Motels retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<ApiResponse<PaginatedResponse<AllMotelsResponse>>> getAllMotels(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        // Validate pagination parameters
        if (page < 0) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("400", null, "Page number cannot be negative")
            );
        }
        
        if (size <= 0 || size > 100) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("400", null, "Page size must be between 1 and 100")
            );
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Motel> motelsPage = motelRepository.findAll(pageable);
        
        List<AllMotelsResponse> responses = allMotelsMapper.toResponseList(motelsPage.getContent());
        PaginatedResponse.PaginationInfo paginationInfo = new PaginatedResponse.PaginationInfo(
            motelsPage.getNumber(),
            motelsPage.getSize(),
            motelsPage.getTotalElements(),
            motelsPage.getTotalPages(),
            motelsPage.isFirst(),
            motelsPage.isLast()
        );
        PaginatedResponse<AllMotelsResponse> paginatedResponse = new PaginatedResponse<>(responses, paginationInfo);
        
        return ResponseEntity.ok(new ApiResponse<>("200", paginatedResponse, "Motels retrieved successfully"));
    }

    @GetMapping("/count")
    @Operation(summary = "Get entity count statistics",
               description = "Retrieve counts of all entities across PostgreSQL tables")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<ApiResponse<AllMotelsCountResponse>> getAllMotelCount() {
        // Get counts from all repositories
        long motelChainsCount = motelChainRepository.count();
        long motelsCount = motelRepository.count();
        long roomCategoriesCount = roomCategoryRepository.count();
        long roomsCount = roomRepository.count();
        
        // Build PostgreSQL tables count map
        Map<String, Long> postgresqlTables = new HashMap<>();
        postgresqlTables.put("motel_chains", motelChainsCount);
        postgresqlTables.put("motels", motelsCount);
        postgresqlTables.put("room_categories", roomCategoriesCount);
        postgresqlTables.put("rooms", roomsCount);
        
        AllMotelsCountResponse countResponse = new AllMotelsCountResponse(
            postgresqlTables,
            motelChainsCount + motelsCount + roomCategoriesCount + roomsCount,
            "MongoDB collections (prices, reservations) are managed by the Go reservation-apis service"
        );
        
        return ResponseEntity.ok(new ApiResponse<>("200", countResponse, "Statistics retrieved successfully"));
    }
}
