package com.example.motels.controller;

import com.example.motels.dto.mapper.RoomMapper;
import com.example.motels.dto.request.CreateRoomRequest;
import com.example.motels.dto.request.UpdateRoomRequest;
import com.example.motels.dto.response.RoomResponse;
import com.example.motels.exception.DuplicateResourceException;
import com.example.motels.exception.ResourceNotFoundException;
import com.example.motels.model.Room;
import com.example.motels.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @MockBean
    private RoomMapper roomMapper;

    private UUID roomId;
    private UUID motelChainId;
    private UUID motelId;
    private UUID categoryId;
    private Room room;
    private RoomResponse roomResponse;
    private CreateRoomRequest createRequest;
    private UpdateRoomRequest updateRequest;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        motelChainId = UUID.randomUUID();
        motelId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        room = new Room();
        room.setRoomId(roomId);
        room.setMotelChainId(motelChainId);
        room.setMotelId(motelId);
        room.setMotelRoomCategoryId(categoryId);
        room.setRoomNumber("101");
        room.setFloor("1");
        room.setStatus("AVAILABLE");
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());

        roomResponse = new RoomResponse();
        roomResponse.setRoomId(roomId);
        roomResponse.setMotelChainId(motelChainId);
        roomResponse.setMotelId(motelId);
        roomResponse.setMotelRoomCategoryId(categoryId);
        roomResponse.setRoomNumber("101");
        roomResponse.setFloor("1");
        roomResponse.setStatus("AVAILABLE");
        roomResponse.setCreatedAt(LocalDateTime.now());
        roomResponse.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateRoomRequest();
        createRequest.setMotelChainId(motelChainId);
        createRequest.setMotelId(motelId);
        createRequest.setMotelRoomCategoryId(categoryId);
        createRequest.setRoomNumber("101");
        createRequest.setFloor("1");
        createRequest.setStatus("AVAILABLE");

        updateRequest = new UpdateRoomRequest();
        updateRequest.setStatus("OCCUPIED");
    }

    @Test
    void createRoom_ValidData_Returns201() throws Exception {
        when(roomMapper.toEntity(any(CreateRoomRequest.class))).thenReturn(room);
        when(roomService.createRoom(any(Room.class))).thenReturn(room);
        when(roomMapper.toResponse(any(Room.class))).thenReturn(roomResponse);

        mockMvc.perform(post("/motelApi/v1/motelRooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("201"))
            .andExpect(jsonPath("$.data.roomId").value(roomId.toString()))
            .andExpect(jsonPath("$.message").value("Room created successfully"));

        verify(roomService, times(1)).createRoom(any(Room.class));
    }

    @Test
    void createRoom_BlankRoomNumber_Returns400() throws Exception {
        createRequest.setRoomNumber("");

        mockMvc.perform(post("/motelApi/v1/motelRooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"));

        verify(roomService, never()).createRoom(any(Room.class));
    }

    @Test
    void createRoom_InvalidStatus_Returns400() throws Exception {
        createRequest.setStatus("INVALID");

        mockMvc.perform(post("/motelApi/v1/motelRooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"));

        verify(roomService, never()).createRoom(any(Room.class));
    }

    @Test
    void createRoom_DuplicateRoomNumber_Returns409() throws Exception {
        when(roomMapper.toEntity(any(CreateRoomRequest.class))).thenReturn(room);
        when(roomService.createRoom(any(Room.class)))
            .thenThrow(new DuplicateResourceException("Room", "room number '101' already exists for this motel"));

        mockMvc.perform(post("/motelApi/v1/motelRooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value("409"));
    }

    @Test
    void getRoomById_ExistingRoom_Returns200() throws Exception {
        when(roomService.getRoomById(roomId)).thenReturn(room);
        when(roomMapper.toResponse(room)).thenReturn(roomResponse);

        mockMvc.perform(get("/motelApi/v1/motelRooms/{roomId}", roomId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.data.roomId").value(roomId.toString()))
            .andExpect(jsonPath("$.data.roomNumber").value("101"));

        verify(roomService, times(1)).getRoomById(roomId);
    }

    @Test
    void getRoomById_NonExistentRoom_Returns404() throws Exception {
        when(roomService.getRoomById(roomId))
            .thenThrow(new ResourceNotFoundException("Room", "id", roomId.toString()));

        mockMvc.perform(get("/motelApi/v1/motelRooms/{roomId}", roomId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    void getAllRooms_WithPagination_Returns200() throws Exception {
        List<Room> rooms = Arrays.asList(room);
        Page<Room> roomPage = new PageImpl<>(rooms, PageRequest.of(0, 10), 1);
        List<RoomResponse> responses = Arrays.asList(roomResponse);

        when(roomService.getAllRoomsWithFilters(any(), any(), any(), any())).thenReturn(roomPage);
        when(roomMapper.toResponseList(rooms)).thenReturn(responses);

        mockMvc.perform(get("/motelApi/v1/motelRooms")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "roomNumber")
                .param("direction", "asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.pagination.page").value(0))
            .andExpect(jsonPath("$.data.pagination.size").value(10));
    }

    @Test
    void getAllRooms_InvalidSortField_Returns400() throws Exception {
        mockMvc.perform(get("/motelApi/v1/motelRooms")
                .param("sort", "invalidField"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid sort field")));
    }

    @Test
    void updateRoom_ValidData_Returns200() throws Exception {
        when(roomService.getRoomById(roomId)).thenReturn(room);
        doNothing().when(roomMapper).updateEntityFromDto(any(UpdateRoomRequest.class), any(Room.class));
        when(roomService.updateRoom(eq(roomId), any(Room.class))).thenReturn(room);
        when(roomMapper.toResponse(room)).thenReturn(roomResponse);

        mockMvc.perform(put("/motelApi/v1/motelRooms/{roomId}", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("Room updated successfully"));

        verify(roomService, times(1)).updateRoom(eq(roomId), any(Room.class));
    }

    @Test
    void updateRoom_NonExistentRoom_Returns404() throws Exception {
        when(roomService.getRoomById(roomId))
            .thenThrow(new ResourceNotFoundException("Room", "id", roomId.toString()));

        mockMvc.perform(put("/motelApi/v1/motelRooms/{roomId}", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    void deleteRoom_ExistingRoom_Returns200() throws Exception {
        doNothing().when(roomService).deleteRoom(roomId);

        mockMvc.perform(delete("/motelApi/v1/motelRooms/{roomId}", roomId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("200"))
            .andExpect(jsonPath("$.message").value("Room deleted successfully"));

        verify(roomService, times(1)).deleteRoom(roomId);
    }

    @Test
    void deleteRoom_NonExistentRoom_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Room", "id", roomId.toString()))
            .when(roomService).deleteRoom(roomId);

        mockMvc.perform(delete("/motelApi/v1/motelRooms/{roomId}", roomId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("404"));
    }
}
