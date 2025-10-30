package com.example.motels.dto.mapper;

import com.example.motels.dto.request.AddressRequest;
import com.example.motels.dto.request.ContactInfoRequest;
import com.example.motels.dto.request.CreateMotelChainRequest;
import com.example.motels.dto.request.UpdateMotelChainRequest;
import com.example.motels.dto.response.MotelChainResponse;
import com.example.motels.model.Address;
import com.example.motels.model.ContactInfo;
import com.example.motels.model.MotelChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MotelChainMapper.
 * Tests DTO ↔ Entity conversion logic.
 * 
 * THEORY: Testing MapStruct Mappers
 * ==================================
 * 
 * Option 1: Mappers.getMapper() (Used here)
 * ==========================================
 * Uses MapStruct's factory to get generated implementation
 * - No Spring context needed
 * - Faster test execution
 * - Perfect for unit tests
 * 
 * mapper = Mappers.getMapper(MotelChainMapper.class);
 * → Returns: MotelChainMapperImpl instance
 * 
 * Option 2: @SpringBootTest + @Autowired (For integration tests)
 * ===============================================================
 * Loads full Spring application context
 * - Tests Spring dependency injection
 * - Slower (database, beans, etc.)
 * - Use for integration tests
 * 
 * How Mappers.getMapper() Works:
 * ==============================
 * 1. MapStruct generates MotelChainMapperImpl at compile-time
 * 2. Mappers.getMapper() uses reflection to instantiate it
 * 3. Returns singleton instance
 * 4. No Spring needed!
 */
@DisplayName("MotelChainMapper Tests")
class MotelChainMapperTest {

    private MotelChainMapper mapper;
    
    @BeforeEach
    void setUp() {
        // Get MapStruct-generated implementation using factory
        mapper = Mappers.getMapper(MotelChainMapper.class);
    }

    // ========== toEntity (CreateRequest → Entity) ==========

    @Test
    @DisplayName("Should convert CreateRequest to Entity with all fields")
    void testToEntity_Success() {
        // Arrange
        CreateMotelChainRequest request = createSampleCreateRequest();

        // Act
        MotelChain entity = mapper.toEntity(request);

        // Assert
        assertNotNull(entity);
        assertEquals("Hilton Hotels", entity.getMotelChainName());
        assertEquals("Hilton", entity.getDisplayName());
        assertEquals("California", entity.getState());
        assertEquals("90001", entity.getPincode());
        assertEquals("ACTIVE", entity.getStatus());
        
        // Verify nested objects
        assertNotNull(entity.getAddress());
        assertEquals("123 Main Street", entity.getAddress().getAddressLine1());
        
        assertNotNull(entity.getContactInfo());
        assertEquals("contact@hilton.com", entity.getContactInfo().getEmail());
        
        // Verify ID and timestamps NOT set (JPA will handle)
        assertNull(entity.getMotelChainId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should return null when CreateRequest is null")
    void testToEntity_NullRequest() {
        // Act
        MotelChain entity = mapper.toEntity((CreateMotelChainRequest) null);

        // Assert
        assertNull(entity);
    }

    @Test
    @DisplayName("Should handle CreateRequest with null nested objects")
    void testToEntity_NullNestedObjects() {
        // Arrange
        CreateMotelChainRequest request = new CreateMotelChainRequest();
        request.setMotelChainName("Test Hotel");
        request.setDisplayName("Test");
        request.setState("CA");
        request.setPincode("90001");
        request.setStatus("ACTIVE");
        request.setAddress(null);
        request.setContactInfo(null);

        // Act
        MotelChain entity = mapper.toEntity(request);

        // Assert
        assertNotNull(entity);
        assertEquals("Test Hotel", entity.getMotelChainName());
        assertNull(entity.getAddress());
        assertNull(entity.getContactInfo());
    }

    // ========== updateEntity (UpdateRequest + Entity → Entity) ==========

    @Test
    @DisplayName("Should update existing entity with UpdateRequest data")
    void testUpdateEntity_Success() {
        // Arrange
        MotelChain existingEntity = createSampleEntity();
        UUID originalId = existingEntity.getMotelChainId();
        LocalDateTime originalCreatedAt = existingEntity.getCreatedAt();

        UpdateMotelChainRequest updateRequest = createSampleUpdateRequest();

        // Act
        mapper.updateEntity(updateRequest, existingEntity);

        // Assert
        assertEquals("Hilton Hotels & Resorts", existingEntity.getMotelChainName());
        assertEquals("Hilton Worldwide", existingEntity.getDisplayName());
        
        // Verify nested objects updated
        assertNotNull(existingEntity.getAddress());
        assertEquals("456 New Address", existingEntity.getAddress().getAddressLine1());
        
        assertNotNull(existingEntity.getContactInfo());
        assertEquals("newemail@hilton.com", existingEntity.getContactInfo().getEmail());
        
        // Verify ID and createdAt NOT changed
        assertEquals(originalId, existingEntity.getMotelChainId());
        assertEquals(originalCreatedAt, existingEntity.getCreatedAt());
    }

    @Test
    @DisplayName("Should not throw exception when UpdateRequest is null")
    void testUpdateEntity_NullRequest() {
        // Arrange
        MotelChain entity = createSampleEntity();

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> mapper.updateEntity(null, entity));
    }

    @Test
    @DisplayName("Should handle null entity gracefully - returns without update")
    void testUpdateEntity_NullEntity() {
        // Arrange
        UpdateMotelChainRequest request = createSampleUpdateRequest();

        // THEORY: MapStruct Behavior with Null Target
        // ============================================
        // MapStruct-generated code checks if REQUEST is null,
        // but doesn't check if ENTITY (@MappingTarget) is null.
        // 
        // This is intentional design:
        // - Null request: Safe to skip (nothing to update)
        // - Null entity: Programming error (can't update nothing!)
        // 
        // Result: Throws NullPointerException (fail-fast behavior)
        // 
        // This is GOOD because:
        // - Catches bugs early in development
        // - Makes code errors obvious
        // - Prevents silent failures
        //
        // In production, entity should NEVER be null because:
        // 1. Service fetches entity from DB first
        // 2. If not found, throws ResourceNotFoundException
        // 3. Only existing entities reach mapper.updateEntity()

        // Act & Assert - MapStruct throws NullPointerException for null entity
        // This is expected and correct behavior (fail-fast)
        assertThrows(NullPointerException.class, 
            () -> mapper.updateEntity(request, null),
            "Should throw NullPointerException when trying to update null entity");
    }

    // ========== toResponse (Entity → Response) ==========

    @Test
    @DisplayName("Should convert Entity to Response with all fields")
    void testToResponse_Success() {
        // Arrange
        MotelChain entity = createSampleEntity();

        // Act
        MotelChainResponse response = mapper.toResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals(entity.getMotelChainId(), response.getMotelChainId());
        assertEquals(entity.getMotelChainName(), response.getMotelChainName());
        assertEquals(entity.getDisplayName(), response.getDisplayName());
        assertEquals(entity.getState(), response.getState());
        assertEquals(entity.getPincode(), response.getPincode());
        assertEquals(entity.getStatus(), response.getStatus());
        
        // Verify timestamps
        assertEquals(entity.getCreatedAt(), response.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), response.getUpdatedAt());
        
        // Verify nested objects
        assertNotNull(response.getAddress());
        assertEquals(entity.getAddress().getAddressLine1(), response.getAddress().getAddressLine1());
        
        assertNotNull(response.getContactInfo());
        assertEquals(entity.getContactInfo().getEmail(), response.getContactInfo().getEmail());
    }

    @Test
    @DisplayName("Should return null when Entity is null")
    void testToResponse_NullEntity() {
        // Act
        MotelChainResponse response = mapper.toResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    @DisplayName("Should handle Entity with null nested objects")
    void testToResponse_NullNestedObjects() {
        // Arrange
        MotelChain entity = new MotelChain();
        entity.setMotelChainId(UUID.randomUUID());
        entity.setMotelChainName("Test Hotel");
        entity.setAddress(null);
        entity.setContactInfo(null);

        // Act
        MotelChainResponse response = mapper.toResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals("Test Hotel", response.getMotelChainName());
        assertNull(response.getAddress());
        assertNull(response.getContactInfo());
    }

    // ========== toResponseList (List<Entity> → List<Response>) ==========

    @Test
    @DisplayName("Should convert list of Entities to list of Responses")
    void testToResponseList_Success() {
        // Arrange
        MotelChain entity1 = createSampleEntity();
        MotelChain entity2 = createSampleEntity();
        entity2.setMotelChainId(UUID.randomUUID());
        entity2.setMotelChainName("Marriott Hotels");

        List<MotelChain> entities = Arrays.asList(entity1, entity2);

        // Act
        List<MotelChainResponse> responses = mapper.toResponseList(entities);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(entity1.getMotelChainName(), responses.get(0).getMotelChainName());
        assertEquals(entity2.getMotelChainName(), responses.get(1).getMotelChainName());
    }

    @Test
    @DisplayName("Should return empty list when input list is empty")
    void testToResponseList_EmptyList() {
        // Act
        List<MotelChainResponse> responses = mapper.toResponseList(List.of());

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should return null when input list is null")
    void testToResponseList_NullList() {
        // Act
        List<MotelChainResponse> responses = mapper.toResponseList(null);

        // Assert
        assertNull(responses);
    }

    @Test
    @DisplayName("Should skip null entities in list")
    void testToResponseList_WithNullEntities() {
        // Arrange
        MotelChain entity1 = createSampleEntity();
        List<MotelChain> entities = Arrays.asList(entity1, null, createSampleEntity());

        // Act
        List<MotelChainResponse> responses = mapper.toResponseList(entities);

        // Assert
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertNotNull(responses.get(0));
        assertNull(responses.get(1));
        assertNotNull(responses.get(2));
    }

    // ========== Helper Methods ==========

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

    private MotelChain createSampleEntity() {
        MotelChain entity = new MotelChain();
        entity.setMotelChainId(UUID.randomUUID());
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
}
