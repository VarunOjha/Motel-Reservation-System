package com.example.motels.controller;

import com.example.motels.model.ApiResponse;
import com.example.motels.model.Motel;
import com.example.motels.service.MotelService;
import com.example.motels.repository.MotelRepository;
import com.example.motels.repository.MotelChainRepository;
import com.example.motels.repository.RoomRepository;
import com.example.motels.repository.RoomCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/motelApi/v1/allMotels")
public class AllMotelsController {

    @Autowired
    private MotelService motelService;

    @Autowired
    private MotelRepository motelRepository;

    @Autowired
    private MotelChainRepository motelChainRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomCategoryRepository roomCategoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllMotels(
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
        
        // Create Pageable object
        Pageable pageable = PageRequest.of(page, size);
        
        // Get paginated results
        Page<Motel> motelsPage = motelService.getAllMotels(pageable);
        
        // Create response with pagination info
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", motelsPage.getContent());
        responseData.put("pagination", Map.of(
            "currentPage", motelsPage.getNumber(),
            "totalPages", motelsPage.getTotalPages(),
            "totalElements", motelsPage.getTotalElements(),
            "pageSize", motelsPage.getSize(),
            "hasNext", motelsPage.hasNext(),
            "hasPrevious", motelsPage.hasPrevious()
        ));
        
        ApiResponse<Map<String, Object>> response = new ApiResponse<>("200", responseData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllMotelCount() {
        Map<String, Object> counts = new HashMap<>();
        
        // Get counts from all repositories
        long motelChainsCount = motelChainRepository.count();
        long motelsCount = motelRepository.count();
        long roomCategoriesCount = roomCategoryRepository.count();
        long roomsCount = roomRepository.count();
        
        // PostgreSQL tables count
        Map<String, Long> postgresqlTables = new HashMap<>();
        postgresqlTables.put("motel_chains", motelChainsCount);
        postgresqlTables.put("motels", motelsCount);
        postgresqlTables.put("room_categories", roomCategoriesCount);
        postgresqlTables.put("rooms", roomsCount);
        
        counts.put("postgresql_tables", postgresqlTables);
        counts.put("total_postgresql_records", motelChainsCount + motelsCount + roomCategoriesCount + roomsCount);
        counts.put("note", "MongoDB collections (prices, reservations) are managed by the Go reservation-apis service");
        
        ApiResponse<Map<String, Object>> response = new ApiResponse<>("200", counts);
        return ResponseEntity.ok(response);
    }
}
