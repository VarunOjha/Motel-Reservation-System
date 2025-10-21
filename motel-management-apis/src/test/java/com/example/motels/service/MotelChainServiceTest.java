package com.example.motels.service;

import com.example.motels.dto.mapper.MotelChainMapper;
import com.example.motels.dto.request.AddressRequest;
import com.example.motels.dto.request.ContactInfoRequest;
import com.example.motels.dto.request.CreateMotelChainRequest;
import com.example.motels.dto.request.UpdateMotelChainRequest;
import com.example.motels.exception.DuplicateResourceException;
import com.example.motels.exception.ResourceNotFoundException;
import com.example.motels.model.Address;
import com.example.motels.model.ContactInfo;
import com.example.motels.model.MotelChain;
import com.example.motels.repository.MotelChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MotelChainService.
 * Tests business logic with mocked repository.
 * 
 * Uses Mockito to mock dependencies.
 * Service returns entities (not DTOs) - controller handles DTO conversion.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MotelChainService Tests")
class MotelChainServiceTest {

    @Mock
    private MotelChainRepository repository;

    @Mock
    private MotelChainMapper mapper;

    @InjectMocks
    private MotelChainService service;

    private UUID testId;
    private MotelChain testEntity;
    private CreateMotelChainRequest createRequest;
    private UpdateMotelChainRequest updateRequest;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testEntity = createSampleEntity(testId);
        createRequest = createSampleCreateRequest();
        updateRequest = createSampleUpdateRequest();
    }

    // ========== CREATE Tests ==========

    @Test
    @DisplayName("Should create MotelChain successfully")
    void testCreateMotelChain_Success() {
        // Arrange
        when(repository.existsByMotelChainNameAndPincodeAndState(
            "Hilton Hotels", "90001", "California"
        )).thenReturn(false);
        
        when(mapper.toEntity(createRequest)).thenReturn(testEntity);
        when(repository.save(testEntity)).thenReturn(testEntity);

        // Act
        MotelChain result = service.createMotelChain(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getMotelChainId());
        assertEquals("Hilton Hotels", result.getMotelChainName());
        
        // Verify interactions
        verify(repository).existsByMotelChainNameAndPincodeAndState(
            "Hilton Hotels", "90001", "California"
        );
        verify(mapper).toEntity(createRequest);
        verify(repository).save(testEntity);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when MotelChain exists")
    void testCreateMotelChain_Duplicate() {
        // Arrange
        when(repository.existsByMotelChainNameAndPincodeAndState(
            "Hilton Hotels", "90001", "California"
        )).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> service.createMotelChain(createRequest)
        );
        
        assertTrue(exception.getMessage().contains("Hilton Hotels"));
        assertTrue(exception.getMessage().contains("California"));

        // Verify repository check was called but save was not
        verify(repository).existsByMotelChainNameAndPincodeAndState(
            "Hilton Hotels", "90001", "California"
        );
        verify(repository, never()).save(any());
        verify(mapper, never()).toEntity(any());
    }

    // ========== GET ALL Tests ==========

    @Test
    @DisplayName("Should get all MotelChains with pagination")
    void testGetAllMotelChains_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        MotelChain entity2 = createSampleEntity(UUID.randomUUID());
        entity2.setMotelChainName("Marriott Hotels");
        
        List<MotelChain> entities = Arrays.asList(testEntity, entity2);
        Page<MotelChain> page = new PageImpl<>(entities, pageable, entities.size());
        
        when(repository.findAll(pageable)).thenReturn(page);

        // Act
        Page<MotelChain> result = service.getAllMotelChains(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals("Hilton Hotels", result.getContent().get(0).getMotelChainName());
        assertEquals("Marriott Hotels", result.getContent().get(1).getMotelChainName());
        
        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("Should return empty page when no MotelChains exist")
    void testGetAllMotelChains_Empty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<MotelChain> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        when(repository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<MotelChain> result = service.getAllMotelChains(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        
        verify(repository).findAll(pageable);
    }

    // ========== GET BY ID Tests ==========

    @Test
    @DisplayName("Should get MotelChain by ID successfully")
    void testGetMotelChainById_Success() {
        // Arrange
        when(repository.findById(testId)).thenReturn(Optional.of(testEntity));

        // Act
        MotelChain result = service.getMotelChainById(testId);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getMotelChainId());
        assertEquals("Hilton Hotels", result.getMotelChainName());
        
        verify(repository).findById(testId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when MotelChain not found")
    void testGetMotelChainById_NotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.getMotelChainById(nonExistentId)
        );
        
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
        assertTrue(exception.getMessage().contains("MotelChain"));

        verify(repository).findById(nonExistentId);
    }

    // ========== UPDATE Tests ==========

    @Test
    @DisplayName("Should update MotelChain successfully")
    void testUpdateMotelChain_Success() {
        // Arrange
        when(repository.findById(testId)).thenReturn(Optional.of(testEntity));
        doNothing().when(mapper).updateEntity(updateRequest, testEntity);
        
        // After update, entity should have updated values
        MotelChain updatedEntity = createSampleEntity(testId);
        updatedEntity.setMotelChainName("Hilton Hotels & Resorts");
        when(repository.save(testEntity)).thenReturn(updatedEntity);

        // Act
        MotelChain result = service.updateMotelChain(testId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getMotelChainId());
        assertEquals("Hilton Hotels & Resorts", result.getMotelChainName());
        
        verify(repository).findById(testId);
        verify(mapper).updateEntity(updateRequest, testEntity);
        verify(repository).save(testEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent MotelChain")
    void testUpdateMotelChain_NotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.updateMotelChain(nonExistentId, updateRequest)
        );
        
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));

        verify(repository).findById(nonExistentId);
        verify(mapper, never()).updateEntity(any(), any());
        verify(repository, never()).save(any());
    }

    // ========== DELETE Tests ==========

    @Test
    @DisplayName("Should delete MotelChain successfully")
    void testDeleteMotelChain_Success() {
        // Arrange
        when(repository.existsById(testId)).thenReturn(true);
        doNothing().when(repository).deleteById(testId);

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> service.deleteMotelChain(testId));

        verify(repository).existsById(testId);
        verify(repository).deleteById(testId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent MotelChain")
    void testDeleteMotelChain_NotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(repository.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.deleteMotelChain(nonExistentId)
        );
        
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));

        verify(repository).existsById(nonExistentId);
        verify(repository, never()).deleteById(any());
    }

    // ========== Edge Case Tests ==========

    @Test
    @DisplayName("Should handle null address in entity creation")
    void testCreateMotelChain_NullAddress() {
        // Arrange
        CreateMotelChainRequest requestWithoutAddress = createSampleCreateRequest();
        requestWithoutAddress.setAddress(null);
        
        MotelChain entityWithoutAddress = createSampleEntity(testId);
        entityWithoutAddress.setAddress(null);
        
        when(repository.existsByMotelChainNameAndPincodeAndState(any(), any(), any()))
            .thenReturn(false);
        when(mapper.toEntity(requestWithoutAddress)).thenReturn(entityWithoutAddress);
        when(repository.save(entityWithoutAddress)).thenReturn(entityWithoutAddress);

        // Act
        MotelChain result = service.createMotelChain(requestWithoutAddress);

        // Assert
        assertNotNull(result);
        assertNull(result.getAddress());
        
        verify(repository).save(entityWithoutAddress);
    }

    @Test
    @DisplayName("Should handle pagination with different page sizes")
    void testGetAllMotelChains_DifferentPageSizes() {
        // Arrange
        Pageable smallPage = PageRequest.of(0, 5);
        List<MotelChain> entities = Arrays.asList(testEntity);
        Page<MotelChain> page = new PageImpl<>(entities, smallPage, 10);
        
        when(repository.findAll(smallPage)).thenReturn(page);

        // Act
        Page<MotelChain> result = service.getAllMotelChains(smallPage);

        // Assert
        assertEquals(5, result.getSize());
        assertEquals(1, result.getNumberOfElements());
        assertEquals(10, result.getTotalElements());
        assertEquals(2, result.getTotalPages()); // 10 total / 5 per page = 2 pages
        
        verify(repository).findAll(smallPage);
    }

    // ========== Helper Methods ==========

    private MotelChain createSampleEntity(UUID id) {
        MotelChain entity = new MotelChain();
        entity.setMotelChainId(id);
        entity.setMotelChainName("Hilton Hotels");
        entity.setDisplayName("Hilton");
        entity.setState("California");
        entity.setPincode("90001");
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now().minusDays(1));
        entity.setUpdatedAt(LocalDateTime.now());

        Address address = new Address();
        address.setAddressLine1("123 Main Street");
        address.setAddressLine2("Suite 500");
        address.setLandmark("Near Central Park");
        address.setAddressName("Headquarters");
        address.setStatus("ACTIVE");
        entity.setAddress(address);

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setPhoneNumber("+12025551234");
        contactInfo.setEmail("contact@hilton.com");
        contactInfo.setContactName("John Doe");
        contactInfo.setContactPosition("Manager");
        contactInfo.setContactType("PRIMARY");
        contactInfo.setContactDescription("Main contact");
        contactInfo.setStatus("ACTIVE");
        entity.setContactInfo(contactInfo);

        return entity;
    }

    private CreateMotelChainRequest createSampleCreateRequest() {
        CreateMotelChainRequest request = new CreateMotelChainRequest();
        request.setMotelChainName("Hilton Hotels");
        request.setDisplayName("Hilton");
        request.setState("California");
        request.setPincode("90001");
        request.setStatus("ACTIVE");

        AddressRequest address = new AddressRequest();
        address.setAddressLine1("123 Main Street");
        address.setAddressLine2("Suite 500");
        address.setLandmark("Near Central Park");
        address.setAddressName("Headquarters");
        address.setStatus("ACTIVE");
        request.setAddress(address);

        ContactInfoRequest contactInfo = new ContactInfoRequest();
        contactInfo.setPhoneNumber("+12025551234");
        contactInfo.setEmail("contact@hilton.com");
        contactInfo.setContactName("John Doe");
        contactInfo.setContactPosition("Manager");
        contactInfo.setContactType("PRIMARY");
        contactInfo.setContactDescription("Main contact");
        contactInfo.setStatus("ACTIVE");
        request.setContactInfo(contactInfo);

        return request;
    }

    private UpdateMotelChainRequest createSampleUpdateRequest() {
        UpdateMotelChainRequest request = new UpdateMotelChainRequest();
        request.setMotelChainName("Hilton Hotels & Resorts");
        request.setDisplayName("Hilton Worldwide");
        request.setState("California");
        request.setPincode("90001");
        request.setStatus("ACTIVE");

        AddressRequest address = new AddressRequest();
        address.setAddressLine1("456 New Address");
        address.setAddressLine2("Floor 10");
        address.setLandmark("Near Beach");
        address.setAddressName("New HQ");
        address.setStatus("ACTIVE");
        request.setAddress(address);

        ContactInfoRequest contactInfo = new ContactInfoRequest();
        contactInfo.setPhoneNumber("+12025559999");
        contactInfo.setEmail("newemail@hilton.com");
        contactInfo.setContactName("Jane Smith");
        contactInfo.setContactPosition("Director");
        contactInfo.setContactType("PRIMARY");
        contactInfo.setContactDescription("Updated contact");
        contactInfo.setStatus("ACTIVE");
        request.setContactInfo(contactInfo);

        return request;
    }
}
