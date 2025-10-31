package com.example.motels.controller;

import com.example.motels.dto.mapper.AllMotelsMapper;
import com.example.motels.dto.response.AllMotelsResponse;
import com.example.motels.model.Motel;
import com.example.motels.repository.MotelChainRepository;
import com.example.motels.repository.MotelRepository;
import com.example.motels.repository.RoomCategoryRepository;
import com.example.motels.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AllMotelsController.class)
class AllMotelsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MotelRepository motelRepository;

    @MockitoBean
    private MotelChainRepository motelChainRepository;

    @MockitoBean
    private RoomRepository roomRepository;

    @MockitoBean
    private RoomCategoryRepository roomCategoryRepository;

    @MockitoBean
    private AllMotelsMapper allMotelsMapper;

    @Test
    void getAllMotels_ValidPagination_Returns200() throws Exception {
        // Arrange
        Motel motel1 = new Motel();
        motel1.setMotelId(UUID.randomUUID());
        motel1.setMotelName("Grand Plaza");
        motel1.setStatus("ACTIVE");

        Motel motel2 = new Motel();
        motel2.setMotelId(UUID.randomUUID());
        motel2.setMotelName("Sunset Inn");
        motel2.setStatus("ACTIVE");

        List<Motel> motels = Arrays.asList(motel1, motel2);
        Page<Motel> motelPage = new PageImpl<>(motels);

        AllMotelsResponse response1 = new AllMotelsResponse();
        response1.setMotelId(motel1.getMotelId());
        response1.setMotelName("Grand Plaza");

        AllMotelsResponse response2 = new AllMotelsResponse();
        response2.setMotelId(motel2.getMotelId());
        response2.setMotelName("Sunset Inn");

        when(motelRepository.findAll(any(Pageable.class))).thenReturn(motelPage);
        when(allMotelsMapper.toResponseList(any())).thenReturn(Arrays.asList(response1, response2));

        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/allMotels")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("Motels retrieved successfully"))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.pagination.page").value(0))
            .andExpect(jsonPath("$.data.pagination.size").value(2));
    }

    @Test
    void getAllMotels_NegativePage_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/allMotels")
                .param("page", "-1")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("Page number cannot be negative"));
    }

    @Test
    void getAllMotels_InvalidSize_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/allMotels")
                .param("page", "0")
                .param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("Page size must be between 1 and 100"));
    }

    @Test
    void getAllMotels_SizeTooLarge_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/allMotels")
                .param("page", "0")
                .param("size", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("Page size must be between 1 and 100"));
    }

    @Test
    void getAllMotels_DefaultPagination_Returns200() throws Exception {
        // Arrange
        List<Motel> motels = Arrays.asList(new Motel());
        Page<Motel> motelPage = new PageImpl<>(motels);

        AllMotelsResponse response = new AllMotelsResponse();
        response.setMotelId(UUID.randomUUID());

        when(motelRepository.findAll(any(Pageable.class))).thenReturn(motelPage);
        when(allMotelsMapper.toResponseList(any())).thenReturn(Arrays.asList(response));

        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/allMotels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.data.pagination.page").value(0))
            .andExpect(jsonPath("$.data.pagination.size").value(1));
    }

    @Test
    void getAllMotelCount_ReturnsStatistics() throws Exception {
        // Arrange
        when(motelChainRepository.count()).thenReturn(5L);
        when(motelRepository.count()).thenReturn(20L);
        when(roomCategoryRepository.count()).thenReturn(10L);
        when(roomRepository.count()).thenReturn(100L);

        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/allMotels/count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("Statistics retrieved successfully"))
            .andExpect(jsonPath("$.data.postgresqlTables.motel_chains").value(5))
            .andExpect(jsonPath("$.data.postgresqlTables.motels").value(20))
            .andExpect(jsonPath("$.data.postgresqlTables.room_categories").value(10))
            .andExpect(jsonPath("$.data.postgresqlTables.rooms").value(100))
            .andExpect(jsonPath("$.data.totalPostgresqlRecords").value(135))
            .andExpect(jsonPath("$.data.note").exists());
    }

    @Test
    void getAllMotelCount_ZeroCounts_ReturnsZero() throws Exception {
        // Arrange
        when(motelChainRepository.count()).thenReturn(0L);
        when(motelRepository.count()).thenReturn(0L);
        when(roomCategoryRepository.count()).thenReturn(0L);
        when(roomRepository.count()).thenReturn(0L);

        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/allMotels/count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.data.totalPostgresqlRecords").value(0));
    }
}
