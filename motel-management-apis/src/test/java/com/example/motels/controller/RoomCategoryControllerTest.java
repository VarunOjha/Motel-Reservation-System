package com.example.motels.controller;

import com.example.motels.dto.mapper.RoomCategoryMapper;
import com.example.motels.dto.request.CreateRoomCategoryRequest;
import com.example.motels.dto.request.UpdateRoomCategoryRequest;
import com.example.motels.dto.response.RoomCategoryResponse;
import com.example.motels.exception.DuplicateResourceException;
import com.example.motels.exception.ResourceNotFoundException;
import com.example.motels.model.RoomCategory;
import com.example.motels.service.RoomCategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomCategoryController.class)
@DisplayName("RoomCategoryController Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomCategoryService service;

    @MockitoBean
    private RoomCategoryMapper mapper;

    private UUID testId;
    private UUID motelChainId;
    private UUID motelId;
    private RoomCategory testEntity;
    private CreateRoomCategoryRequest createRequest;
    private UpdateRoomCategoryRequest updateRequest;
    private RoomCategoryResponse response;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        motelChainId = UUID.randomUUID();
        motelId = UUID.randomUUID();

        testEntity = new RoomCategory();
        testEntity.setMotelRoomCategoryId(testId);
        testEntity.setMotelChainId(motelChainId);
        testEntity.setMotelId(motelId);
        testEntity.setRoomCategoryName("Deluxe Suite");
        testEntity.setDisplayName("Deluxe Suite - Ocean View");
        testEntity.setDescription("Luxury room with ocean view");
        testEntity.setStatus("ACTIVE");
        testEntity.setCreatedAt(LocalDateTime.now());
        testEntity.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateRoomCategoryRequest(
            motelChainId,
            motelId,
            "Deluxe Suite",
            "Deluxe Suite - Ocean View",
            "Luxury room with ocean view",
            "ACTIVE"
        );

        updateRequest = new UpdateRoomCategoryRequest(
            "Premium Suite",
            "Premium Suite - City View",
            "Updated description",
            "ACTIVE"
        );

        response = new RoomCategoryResponse(
            testId,
            motelChainId,
            motelId,
            "Deluxe Suite",
            "Deluxe Suite - Ocean View",
            "Luxury room with ocean view",
            "ACTIVE",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create room category with valid data")
    void testCreateRoomCategory_Success() throws Exception {
        when(mapper.toEntity(any(CreateRoomCategoryRequest.class))).thenReturn(testEntity);
        when(service.createRoomCategory(any(RoomCategory.class))).thenReturn(testEntity);
        when(mapper.toResponse(any(RoomCategory.class))).thenReturn(response);

        mockMvc.perform(post("/motelApi/v1/motelRoomCategories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("201"))
            .andExpect(jsonPath("$.data.roomCategoryName").value("Deluxe Suite"))
            .andExpect(jsonPath("$.correlationId").exists())
            .andExpect(jsonPath("$.timestamp").exists());

        verify(service, times(1)).createRoomCategory(any(RoomCategory.class));
    }

    @Test
    @DisplayName("Should reject creation with blank category name")
    void testCreateRoomCategory_BlankName() throws Exception {
        CreateRoomCategoryRequest invalidRequest = new CreateRoomCategoryRequest(
            motelChainId, motelId, "", "Display", "Description", "ACTIVE"
        );

        mockMvc.perform(post("/motelApi/v1/motelRoomCategories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"));

        verify(service, never()).createRoomCategory(any());
    }

    @Test
    @DisplayName("Should reject creation with invalid status")
    void testCreateRoomCategory_InvalidStatus() throws Exception {
        CreateRoomCategoryRequest invalidRequest = new CreateRoomCategoryRequest(
            motelChainId, motelId, "Suite", "Display", "Description", "INVALID_STATUS"
        );

        mockMvc.perform(post("/motelApi/v1/motelRoomCategories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(service, never()).createRoomCategory(any());
    }

    @Test
    @DisplayName("Should handle duplicate category name")
    void testCreateRoomCategory_Duplicate() throws Exception {
        when(mapper.toEntity(any(CreateRoomCategoryRequest.class))).thenReturn(testEntity);
        when(service.createRoomCategory(any(RoomCategory.class)))
            .thenThrow(new DuplicateResourceException("RoomCategory", "name already exists"));

        mockMvc.perform(post("/motelApi/v1/motelRoomCategories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value("409"));
    }

    @Test
    @DisplayName("Should get room category by ID")
    void testGetRoomCategoryById_Success() throws Exception {
        when(service.getRoomCategoryById(testId)).thenReturn(testEntity);
        when(mapper.toResponse(testEntity)).thenReturn(response);

        mockMvc.perform(get("/motelApi/v1/motelRoomCategories/{id}", testId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.data.motelRoomCategoryId").value(testId.toString()))
            .andExpect(jsonPath("$.data.roomCategoryName").value("Deluxe Suite"));

        verify(service, times(1)).getRoomCategoryById(testId);
    }

    @Test
    @DisplayName("Should return 404 when category not found")
    void testGetRoomCategoryById_NotFound() throws Exception {
        when(service.getRoomCategoryById(testId))
            .thenThrow(new ResourceNotFoundException("RoomCategory", "id", testId));

        mockMvc.perform(get("/motelApi/v1/motelRoomCategories/{id}", testId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    @DisplayName("Should update room category")
    void testUpdateRoomCategory_Success() throws Exception {
        when(service.getRoomCategoryById(testId)).thenReturn(testEntity);
        when(service.updateRoomCategory(eq(testId), any(RoomCategory.class))).thenReturn(testEntity);
        when(mapper.toResponse(any(RoomCategory.class))).thenReturn(response);

        mockMvc.perform(put("/motelApi/v1/motelRoomCategories/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("Room category updated successfully"));

        verify(service, times(1)).updateRoomCategory(eq(testId), any(RoomCategory.class));
    }

    @Test
    @DisplayName("Should delete room category")
    void testDeleteRoomCategory_Success() throws Exception {
        doNothing().when(service).deleteRoomCategory(testId);

        mockMvc.perform(delete("/motelApi/v1/motelRoomCategories/{id}", testId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("Room category deleted successfully"));

        verify(service, times(1)).deleteRoomCategory(testId);
    }

    @Test
    @DisplayName("Should get all room categories with pagination")
    void testGetAllRoomCategories_Success() throws Exception {
        List<RoomCategory> categories = Arrays.asList(testEntity);
        Page<RoomCategory> page = new PageImpl<>(categories);
        
        when(service.getAllRoomCategoriesWithFilters(any(), any(), any(), any())).thenReturn(page);
        when(mapper.toResponseList(anyList())).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/motelApi/v1/motelRoomCategories")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.pagination").exists());
    }

    @Test
    @DisplayName("Should reject invalid pagination parameters")
    void testGetAllRoomCategories_InvalidPagination() throws Exception {
        mockMvc.perform(get("/motelApi/v1/motelRoomCategories")
                .param("page", "-1")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"));

        verify(service, never()).getAllRoomCategoriesWithFilters(any(), any(), any(), any());
    }
}
