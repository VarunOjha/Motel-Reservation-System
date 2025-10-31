package com.example.motels.dto.mapper;

import com.example.motels.dto.request.CreateRoomRequest;
import com.example.motels.dto.request.UpdateRoomRequest;
import com.example.motels.dto.response.RoomResponse;
import com.example.motels.model.Room;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Room entity and DTOs.
 * Provides compile-time bean mapping with zero runtime overhead.
 */
@Mapper(componentModel = "spring")
public interface RoomMapper {

    /**
     * Convert CreateRoomRequest DTO to Room entity.
     */
    Room toEntity(CreateRoomRequest request);

    /**
     * Update existing Room entity from UpdateRoomRequest DTO.
     * Only non-null fields from the DTO will be mapped (partial update).
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateRoomRequest request, @MappingTarget Room room);

    /**
     * Convert Room entity to RoomResponse DTO.
     */
    RoomResponse toResponse(Room room);

    /**
     * Convert list of Room entities to list of RoomResponse DTOs.
     */
    List<RoomResponse> toResponseList(List<Room> rooms);
}
