package com.example.motels.dto.mapper;

import com.example.motels.dto.request.AddressRequest;
import com.example.motels.dto.request.ContactInfoRequest;
import com.example.motels.dto.request.CreateMotelChainRequest;
import com.example.motels.dto.request.UpdateMotelChainRequest;
import com.example.motels.dto.response.AddressResponse;
import com.example.motels.dto.response.ContactInfoResponse;
import com.example.motels.dto.response.MotelChainResponse;
import com.example.motels.model.Address;
import com.example.motels.model.ContactInfo;
import com.example.motels.model.MotelChain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between MotelChain entities and DTOs.
 * Using manual mapping for learning purposes and full control.
 * 
 * Benefits of manual mapping:
 * - Easy to debug (no reflection magic)
 * - Explicit field mapping (no surprises)
 * - Good for learning the pattern
 * 
 * For production, consider MapStruct for compile-time code generation.
 */
@Component
public class MotelChainMapper {
    
    // ========== Request DTOs → Entity ==========
    
    /**
     * Converts CreateMotelChainRequest to MotelChain entity.
     * Used when creating a new motel chain.
     * Does NOT set id, createdAt, updatedAt (handled by JPA).
     */
    public MotelChain toEntity(CreateMotelChainRequest request) {
        if (request == null) {
            return null;
        }
        
        MotelChain entity = new MotelChain();
        entity.setMotelChainName(request.getMotelChainName());
        entity.setDisplayName(request.getDisplayName());
        entity.setState(request.getState());
        entity.setPincode(request.getPincode());
        entity.setStatus(request.getStatus());
        
        // Map nested objects
        entity.setAddress(toAddressEntity(request.getAddress()));
        entity.setContactInfo(toContactInfoEntity(request.getContactInfo()));
        
        return entity;
    }
    
    /**
     * Updates an existing MotelChain entity with data from UpdateMotelChainRequest.
     * Used for PUT operations.
     * Preserves id, timestamps, and other managed fields.
     * 
     * @param request The update request with new values
     * @param entity The existing entity to update (JPA-managed)
     */
    public void updateEntity(UpdateMotelChainRequest request, MotelChain entity) {
        if (request == null || entity == null) {
            return;
        }
        
        entity.setMotelChainName(request.getMotelChainName());
        entity.setDisplayName(request.getDisplayName());
        entity.setState(request.getState());
        entity.setPincode(request.getPincode());
        entity.setStatus(request.getStatus());
        
        // Update nested objects
        entity.setAddress(toAddressEntity(request.getAddress()));
        entity.setContactInfo(toContactInfoEntity(request.getContactInfo()));
        
        // Note: Do NOT modify motelChainId, createdAt, updatedAt, deletedAt
        // JPA manages these automatically
    }
    
    // ========== Entity → Response DTOs ==========
    
    /**
     * Converts MotelChain entity to MotelChainResponse DTO.
     * Used for all GET operations (list and detail).
     */
    public MotelChainResponse toResponse(MotelChain entity) {
        if (entity == null) {
            return null;
        }
        
        MotelChainResponse response = new MotelChainResponse();
        response.setMotelChainId(entity.getMotelChainId());
        response.setMotelChainName(entity.getMotelChainName());
        response.setDisplayName(entity.getDisplayName());
        response.setState(entity.getState());
        response.setPincode(entity.getPincode());
        response.setStatus(entity.getStatus());
        
        // Map nested objects
        response.setAddress(toAddressResponse(entity.getAddress()));
        response.setContactInfo(toContactInfoResponse(entity.getContactInfo()));
        
        // Include timestamps for debugging/caching
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        
        // Explicitly exclude deletedAt (internal field)
        
        return response;
    }
    
    /**
     * Converts a list of MotelChain entities to a list of response DTOs.
     * Used for paginated list endpoints.
     */
    public List<MotelChainResponse> toResponseList(List<MotelChain> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    // ========== Nested Object Mapping ==========
    
    private Address toAddressEntity(AddressRequest request) {
        if (request == null) {
            return null;
        }
        
        Address address = new Address();
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setAddressName(request.getAddressName());
        address.setStatus(request.getStatus());
        return address;
    }
    
    private AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }
        
        AddressResponse response = new AddressResponse();
        response.setAddressLine1(address.getAddressLine1());
        response.setAddressLine2(address.getAddressLine2());
        response.setLandmark(address.getLandmark());
        response.setAddressName(address.getAddressName());
        response.setStatus(address.getStatus());
        return response;
    }
    
    private ContactInfo toContactInfoEntity(ContactInfoRequest request) {
        if (request == null) {
            return null;
        }
        
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setPhoneNumber(request.getPhoneNumber());
        contactInfo.setEmail(request.getEmail());
        contactInfo.setContactName(request.getContactName());
        contactInfo.setContactPosition(request.getContactPosition());
        contactInfo.setContactType(request.getContactType());
        contactInfo.setContactDescription(request.getContactDescription());
        contactInfo.setStatus(request.getStatus());
        return contactInfo;
    }
    
    private ContactInfoResponse toContactInfoResponse(ContactInfo contactInfo) {
        if (contactInfo == null) {
            return null;
        }
        
        ContactInfoResponse response = new ContactInfoResponse();
        response.setPhoneNumber(contactInfo.getPhoneNumber());
        response.setEmail(contactInfo.getEmail());
        response.setContactName(contactInfo.getContactName());
        response.setContactPosition(contactInfo.getContactPosition());
        response.setContactType(contactInfo.getContactType());
        response.setContactDescription(contactInfo.getContactDescription());
        response.setStatus(contactInfo.getStatus());
        return response;
    }
}
