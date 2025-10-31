package com.example.motels.dto.mapper;

import com.example.motels.dto.response.AllMotelsResponse;
import com.example.motels.model.Motel;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for AllMotels API.
 * Converts Motel entities to AllMotelsResponse DTOs.
 */
@Mapper(componentModel = "spring")
public interface AllMotelsMapper {

    /**
     * Convert Motel entity to AllMotelsResponse DTO.
     */
    AllMotelsResponse toResponse(Motel motel);

    /**
     * Convert list of Motel entities to list of AllMotelsResponse DTOs.
     */
    List<AllMotelsResponse> toResponseList(List<Motel> motels);
}
