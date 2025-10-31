package com.example.motels.controller;

import com.example.motels.dto.mapper.MotelMapper;
import com.example.motels.dto.request.CreateMotelRequest;
import com.example.motels.dto.request.UpdateMotelRequest;
import com.example.motels.dto.response.MotelResponse;
import com.example.motels.exception.DuplicateResourceException;
import com.example.motels.exception.ResourceNotFoundException;
import com.example.motels.model.Motel;
import com.example.motels.service.MotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MotelController.class)
class MotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MotelService motelService;

    @MockitoBean
    private MotelMapper motelMapper;

    private UUID motelId;
    private String motelChainId;
    private Motel motel;
    private MotelResponse motelResponse;
    private CreateMotelRequest createRequest;
    private UpdateMotelRequest updateRequest;

    @BeforeEach
    void setUp() {
        motelId = UUID.randomUUID();
        motelChainId = "f22bdf78-4713-4292-ac50-a2be85155115";

        motel = new Motel();
        motel.setMotelId(motelId);
        motel.setMotelChainId(motelChainId);
        motel.setMotelName("Grand Plaza Hotel");
        motel.setStatus("ACTIVE");
        motel.setPincode("560001");
        motel.setState("Karnataka");
        motel.setCreatedAt(LocalDateTime.now());
        motel.setUpdatedAt(LocalDateTime.now());

        motelResponse = new MotelResponse();
        motelResponse.setMotelId(motelId);
        motelResponse.setMotelChainId(motelChainId);
        motelResponse.setMotelName("Grand Plaza Hotel");
        motelResponse.setStatus("ACTIVE");
        motelResponse.setPincode("560001");
        motelResponse.setState("Karnataka");
        motelResponse.setCreatedAt(LocalDateTime.now());
        motelResponse.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateMotelRequest();
        createRequest.setMotelChainId(motelChainId);
        createRequest.setMotelName("Grand Plaza Hotel");
        createRequest.setStatus("ACTIVE");
        createRequest.setPincode("560001");
        createRequest.setState("Karnataka");

        updateRequest = new UpdateMotelRequest();
        updateRequest.setStatus("INACTIVE");
    }

    @Test
    void createMotel_ValidData_Returns201() throws Exception {
        when(motelMapper.toEntity(any(CreateMotelRequest.class))).thenReturn(motel);
        when(motelService.createMotel(any(Motel.class))).thenReturn(motel);
        when(motelMapper.toResponse(any(Motel.class))).thenReturn(motelResponse);

        mockMvc.perform(post("/motelApi/v1/motels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("201"))
            .andExpect(jsonPath("$.data.motelId").value(motelId.toString()))
            .andExpect(jsonPath("$.message").value("Motel created successfully"));

        verify(motelService, times(1)).createMotel(any(Motel.class));
    }

    @Test
    void createMotel_BlankMotelName_Returns400() throws Exception {
        createRequest.setMotelName("");

        mockMvc.perform(post("/motelApi/v1/motels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"));

        verify(motelService, never()).createMotel(any(Motel.class));
    }

    @Test
    void createMotel_InvalidUUID_Returns400() throws Exception {
        createRequest.setMotelChainId("invalid-uuid");

        mockMvc.perform(post("/motelApi/v1/motels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"));

        verify(motelService, never()).createMotel(any(Motel.class));
    }

    @Test
    void createMotel_DuplicateMotel_Returns409() throws Exception {
        when(motelMapper.toEntity(any(CreateMotelRequest.class))).thenReturn(motel);
        when(motelService.createMotel(any(Motel.class)))
            .thenThrow(new DuplicateResourceException("Motel", "motel 'Grand Plaza Hotel' already exists"));

        mockMvc.perform(post("/motelApi/v1/motels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value("409"));
    }

    @Test
    void getMotelById_ExistingMotel_Returns200() throws Exception {
        when(motelService.getMotelById(motelId)).thenReturn(motel);
        when(motelMapper.toResponse(motel)).thenReturn(motelResponse);

        mockMvc.perform(get("/motelApi/v1/motels/{motelId}", motelId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.data.motelId").value(motelId.toString()))
            .andExpect(jsonPath("$.data.motelName").value("Grand Plaza Hotel"));

        verify(motelService, times(1)).getMotelById(motelId);
    }

    @Test
    void getMotelById_NonExistentMotel_Returns404() throws Exception {
        when(motelService.getMotelById(motelId))
            .thenThrow(new ResourceNotFoundException("Motel", "id", motelId.toString()));

        mockMvc.perform(get("/motelApi/v1/motels/{motelId}", motelId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    void getAllMotels_WithPagination_Returns200() throws Exception {
        List<Motel> motels = Arrays.asList(motel);
        Page<Motel> motelPage = new PageImpl<>(motels, PageRequest.of(0, 10), 1);
        List<MotelResponse> responses = Arrays.asList(motelResponse);

        when(motelService.getMotelsWithFilters(any(), any(), any(), any(), any(), any())).thenReturn(motelPage);
        when(motelMapper.toResponseList(motels)).thenReturn(responses);

        mockMvc.perform(get("/motelApi/v1/motels")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "motelName")
                .param("direction", "asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.pagination.page").value(0))
            .andExpect(jsonPath("$.data.pagination.size").value(10));
    }

    @Test
    void getAllMotels_InvalidSortField_Returns400() throws Exception {
        mockMvc.perform(get("/motelApi/v1/motels")
                .param("sort", "invalidField"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid sort field")));
    }

    @Test
    void updateMotel_ValidData_Returns200() throws Exception {
        when(motelService.getMotelById(motelId)).thenReturn(motel);
        doNothing().when(motelMapper).updateEntityFromDto(any(UpdateMotelRequest.class), any(Motel.class));
        when(motelService.updateMotel(any(Motel.class))).thenReturn(motel);
        when(motelMapper.toResponse(motel)).thenReturn(motelResponse);

        mockMvc.perform(put("/motelApi/v1/motels/{motelId}", motelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("Motel updated successfully"));

        verify(motelService, times(1)).updateMotel(any(Motel.class));
    }

    @Test
    void updateMotel_NonExistentMotel_Returns404() throws Exception {
        when(motelService.getMotelById(motelId))
            .thenThrow(new ResourceNotFoundException("Motel", "id", motelId.toString()));

        mockMvc.perform(put("/motelApi/v1/motels/{motelId}", motelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    void deleteMotel_ExistingMotel_Returns200() throws Exception {
        doNothing().when(motelService).deleteMotel(motelId);

        mockMvc.perform(delete("/motelApi/v1/motels/{motelId}", motelId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("Motel deleted successfully"));

        verify(motelService, times(1)).deleteMotel(motelId);
    }

    @Test
    void deleteMotel_NonExistentMotel_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Motel", "id", motelId.toString()))
            .when(motelService).deleteMotel(motelId);

        mockMvc.perform(delete("/motelApi/v1/motels/{motelId}", motelId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("404"));
    }
}
