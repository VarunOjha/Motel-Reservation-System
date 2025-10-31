package com.example.motels.dto.mapper;

import com.example.motels.dto.request.CreateMotelRequest;
import com.example.motels.dto.request.UpdateMotelRequest;
import com.example.motels.dto.response.MotelResponse;
import com.example.motels.model.Motel;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Motel entity and DTOs.
 * Provides compile-time bean mapping with zero runtime overhead.
 */
@Mapper(componentModel = "spring")
public interface MotelMapper {

    /**
     * Convert CreateMotelRequest DTO to Motel entity.
     */
    Motel toEntity(CreateMotelRequest request);

    /**
     * Update existing Motel entity from UpdateMotelRequest DTO.
     * Only non-null fields from the DTO will be mapped (partial update).
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateMotelRequest request, @MappingTarget Motel motel);

    /**
     * Convert Motel entity to MotelResponse DTO.
     */
    MotelResponse toResponse(Motel motel);

    /**
     * Convert list of Motel entities to list of MotelResponse DTOs.
     */
    List<MotelResponse> toResponseList(List<Motel> motels);
}
