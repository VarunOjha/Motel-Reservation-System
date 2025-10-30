package com.example.motels.controller;

import com.example.motels.dto.mapper.MotelChainMapper;
import com.example.motels.dto.request.AddressRequest;
import com.example.motels.dto.request.ContactInfoRequest;
import com.example.motels.dto.request.CreateMotelChainRequest;
import com.example.motels.dto.request.UpdateMotelChainRequest;
import com.example.motels.dto.response.MotelChainResponse;
import com.example.motels.exception.DuplicateResourceException;
import com.example.motels.exception.ResourceNotFoundException;
import com.example.motels.model.Address;
import com.example.motels.model.ContactInfo;
import com.example.motels.model.MotelChain;
import com.example.motels.service.MotelChainService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for MotelChainController.
 * Uses MockMvc to test HTTP layer without starting full server.
 * 
 * @WebMvcTest - Loads only web layer (controllers, filters, etc.)
 * @MockitoBean - Mocks the service layer (replaces deprecated @MockBean)
 */
@WebMvcTest(MotelChainController.class)
@DisplayName("MotelChainController Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class MotelChainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MotelChainService service;

    @MockitoBean
    private MotelChainMapper mapper;

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

    // ========== POST /motelApi/v1/motelChains (CREATE) ==========

    @Test
    @DisplayName("POST /motelChains - Should create MotelChain and return 201")
    void testCreateMotelChain_Success() throws Exception {
        // Arrange
        MotelChainResponse response = createSampleResponse(testId);
        
        when(service.createMotelChain(any(CreateMotelChainRequest.class)))
            .thenReturn(testEntity);
        when(mapper.toResponse(testEntity)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/motelApi/v1/motelChains")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("201"))
                .andExpect(jsonPath("$.data.motelChainId").value(testId.toString()))
                .andExpect(jsonPath("$.data.motelChainName").value("Hilton Hotels"))
                .andExpect(jsonPath("$.data.state").value("California"))
                .andExpect(jsonPath("$.data.address").exists())
                .andExpect(jsonPath("$.data.contactInfo").exists());

        verify(service).createMotelChain(any(CreateMotelChainRequest.class));
        verify(mapper).toResponse(testEntity);
    }

    @Test
    @DisplayName("POST /motelChains - Should return 400 for validation errors")
    void testCreateMotelChain_ValidationErrors() throws Exception {
        // Arrange - Request with missing required fields
        CreateMotelChainRequest invalidRequest = new CreateMotelChainRequest();
        invalidRequest.setDisplayName("Test");
        // motelChainName is missing

        // Act & Assert
        mockMvc.perform(post("/motelApi/v1/motelChains")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(service, never()).createMotelChain(any());
    }

    @Test
    @DisplayName("POST /motelChains - Should return 409 for duplicate")
    void testCreateMotelChain_Duplicate() throws Exception {
        // Arrange
        when(service.createMotelChain(any(CreateMotelChainRequest.class)))
            .thenThrow(new DuplicateResourceException(
                "MotelChain",
                "name='Hilton Hotels', state='California', pincode='90001'"
            ));

        // Act & Assert
        mockMvc.perform(post("/motelApi/v1/motelChains")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("409"))
                .andExpect(jsonPath("$.message").value(containsString("already exists")));

        verify(service).createMotelChain(any(CreateMotelChainRequest.class));
    }

    // ========== GET /motelApi/v1/motelChains (GET ALL) ==========

    @Test
    @DisplayName("GET /motelChains - Should return paginated list")
    void testGetAllMotelChains_Success() throws Exception {
        // Arrange
        UUID id2 = UUID.randomUUID();
        MotelChain entity2 = createSampleEntity(id2);
        entity2.setMotelChainName("Marriott Hotels");
        
        List<MotelChain> entities = Arrays.asList(testEntity, entity2);
        Page<MotelChain> page = new PageImpl<>(entities, PageRequest.of(0, 10), 2);
        
        MotelChainResponse response1 = createSampleResponse(testId);
        MotelChainResponse response2 = createSampleResponse(id2);
        response2.setMotelChainName("Marriott Hotels");
        List<MotelChainResponse> responses = Arrays.asList(response1, response2);
        
        when(service.getAllMotelChains(any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseList(entities)).thenReturn(responses);

        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/motelChains")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "createdAt")
                .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].motelChainName").value("Hilton Hotels"))
                .andExpect(jsonPath("$.data.content[1].motelChainName").value("Marriott Hotels"))
                .andExpect(jsonPath("$.data.pagination.total_elements").value(2))
                .andExpect(jsonPath("$.data.pagination.page").value(0))
                .andExpect(jsonPath("$.data.pagination.size").value(10));

        verify(service).getAllMotelChains(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /motelChains - Should return empty list when no data")
    void testGetAllMotelChains_Empty() throws Exception {
        // Arrange
        Page<MotelChain> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(service.getAllMotelChains(any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/motelChains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.pagination.total_elements").value(0));

        verify(service).getAllMotelChains(any(Pageable.class));
    }

    // ========== GET /motelApi/v1/motelChains/{id} (GET BY ID) ==========

    @Test
    @DisplayName("GET /motelChains/{id} - Should return MotelChain")
    void testGetMotelChainById_Success() throws Exception {
        // Arrange
        MotelChainResponse response = createSampleResponse(testId);
        
        when(service.getMotelChainById(testId)).thenReturn(testEntity);
        when(mapper.toResponse(testEntity)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/motelChains/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.motelChainId").value(testId.toString()))
                .andExpect(jsonPath("$.data.motelChainName").value("Hilton Hotels"))
                .andExpect(jsonPath("$.data.state").value("California"));

        verify(service).getMotelChainById(testId);
    }

    @Test
    @DisplayName("GET /motelChains/{id} - Should return 404 when not found")
    void testGetMotelChainById_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(service.getMotelChainById(nonExistentId))
            .thenThrow(new ResourceNotFoundException("MotelChain", "motelChainId", nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/motelChains/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message").value(containsString("not found")));

        verify(service).getMotelChainById(nonExistentId);
    }

    @Test
    @DisplayName("GET /motelChains/{id} - Should return 500 for invalid UUID format")
    void testGetMotelChainById_InvalidUUID() throws Exception {
        // Note: Spring converts UUID format exceptions to 500 by default
        // To get 400, we'd need custom exception handling for IllegalArgumentException
        
        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/motelChains/{id}", "invalid-uuid"))
                .andExpect(status().isInternalServerError());

        verify(service, never()).getMotelChainById(any());
    }

    // ========== PUT /motelApi/v1/motelChains/{id} (UPDATE) ==========

    @Test
    @DisplayName("PUT /motelChains/{id} - Should update MotelChain")
    void testUpdateMotelChain_Success() throws Exception {
        // Arrange
        MotelChain updatedEntity = createSampleEntity(testId);
        updatedEntity.setMotelChainName("Hilton Hotels & Resorts");
        
        MotelChainResponse response = createSampleResponse(testId);
        response.setMotelChainName("Hilton Hotels & Resorts");
        
        when(service.updateMotelChain(eq(testId), any(UpdateMotelChainRequest.class)))
            .thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/motelApi/v1/motelChains/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data.motelChainId").value(testId.toString()))
                .andExpect(jsonPath("$.data.motelChainName").value("Hilton Hotels & Resorts"));

        verify(service).updateMotelChain(eq(testId), any(UpdateMotelChainRequest.class));
    }

    @Test
    @DisplayName("PUT /motelChains/{id} - Should return 404 when not found")
    void testUpdateMotelChain_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(service.updateMotelChain(eq(nonExistentId), any(UpdateMotelChainRequest.class)))
            .thenThrow(new ResourceNotFoundException("MotelChain", "motelChainId", nonExistentId));

        // Act & Assert
        mockMvc.perform(put("/motelApi/v1/motelChains/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"));

        verify(service).updateMotelChain(eq(nonExistentId), any(UpdateMotelChainRequest.class));
    }

    @Test
    @DisplayName("PUT /motelChains/{id} - Should return 400 for validation errors")
    void testUpdateMotelChain_ValidationErrors() throws Exception {
        // Arrange - Invalid request (e.g., empty name)
        UpdateMotelChainRequest invalidRequest = new UpdateMotelChainRequest();
        invalidRequest.setState("CA");
        // motelChainName is missing

        // Act & Assert
        mockMvc.perform(put("/motelApi/v1/motelChains/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(service, never()).updateMotelChain(any(), any());
    }

    // ========== DELETE /motelApi/v1/motelChains/{id} (DELETE) ==========

    @Test
    @DisplayName("DELETE /motelChains/{id} - Should delete MotelChain")
    void testDeleteMotelChain_Success() throws Exception {
        // Arrange
        doNothing().when(service).deleteMotelChain(testId);

        // Act & Assert
        mockMvc.perform(delete("/motelApi/v1/motelChains/{id}", testId))
                .andExpect(status().isNoContent());

        verify(service).deleteMotelChain(testId);
    }

    @Test
    @DisplayName("DELETE /motelChains/{id} - Should return 404 when not found")
    void testDeleteMotelChain_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("MotelChain", "motelChainId", nonExistentId))
            .when(service).deleteMotelChain(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/motelApi/v1/motelChains/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"));

        verify(service).deleteMotelChain(nonExistentId);
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("POST /motelChains - Should return 500 for malformed JSON")
    void testCreateMotelChain_MalformedJSON() throws Exception {
        // Note: Spring returns 500 for JSON parse errors by default
        // To get 400, we'd need HttpMessageNotReadableException handling in GlobalExceptionHandler
        
        // Act & Assert
        mockMvc.perform(post("/motelApi/v1/motelChains")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isInternalServerError());

        verify(service, never()).createMotelChain(any());
    }

    @Test
    @DisplayName("GET /motelChains - Should handle custom pagination parameters")
    void testGetAllMotelChains_CustomPagination() throws Exception {
        // Arrange
        Page<MotelChain> page = new PageImpl<>(
            List.of(testEntity), 
            PageRequest.of(2, 5), 
            20
        );
        when(service.getAllMotelChains(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/motelApi/v1/motelChains")
                .param("page", "2")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.page").value(2))
                .andExpect(jsonPath("$.data.pagination.size").value(5))
                .andExpect(jsonPath("$.data.pagination.total_pages").value(4));

        verify(service).getAllMotelChains(any(Pageable.class));
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

    private MotelChainResponse createSampleResponse(UUID id) {
        MotelChainResponse response = new MotelChainResponse();
        response.setMotelChainId(id);
        response.setMotelChainName("Hilton Hotels");
        response.setDisplayName("Hilton");
        response.setState("California");
        response.setPincode("90001");
        response.setStatus("ACTIVE");
        response.setCreatedAt(LocalDateTime.now().minusDays(1));
        response.setUpdatedAt(LocalDateTime.now());

        com.example.motels.dto.response.AddressResponse address = 
            new com.example.motels.dto.response.AddressResponse();
        address.setAddressLine1("123 Main Street");
        address.setAddressLine2("Suite 500");
        address.setLandmark("Near Central Park");
        address.setAddressName("Headquarters");
        address.setStatus("ACTIVE");
        response.setAddress(address);

        com.example.motels.dto.response.ContactInfoResponse contactInfo = 
            new com.example.motels.dto.response.ContactInfoResponse();
        contactInfo.setPhoneNumber("+12025551234");
        contactInfo.setEmail("contact@hilton.com");
        contactInfo.setContactName("John Doe");
        contactInfo.setContactPosition("Manager");
        contactInfo.setContactType("PRIMARY");
        contactInfo.setContactDescription("Main contact");
        contactInfo.setStatus("ACTIVE");
        response.setContactInfo(contactInfo);

        return response;
    }
}
