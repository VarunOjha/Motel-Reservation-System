package com.example.motels.service;


import com.example.motels.dto.mapper.MotelChainMapper;
import com.example.motels.dto.request.CreateMotelChainRequest;
import com.example.motels.dto.request.UpdateMotelChainRequest;
import com.example.motels.dto.response.MotelChainResponse;
import com.example.motels.exception.DuplicateResourceException;
import com.example.motels.exception.ResourceNotFoundException;
import com.example.motels.model.MotelChain;
import com.example.motels.repository.MotelChainRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for MotelChain business logic.
 * Now uses DTOs instead of entities for decoupling from persistence layer.
 * 
 * Key improvements:
 * - DTOs for input/output (API contract decoupled from database)
 * - Custom exceptions for better error handling
 * - @Transactional for data consistency
 * - Automatic logging via AOP (see LoggingAspect)
 */
@Service
@Transactional(readOnly = true) // Default: Read-only transactions for performance
public class MotelChainService {

    private final MotelChainRepository motelChainRepository;
    private final MotelChainMapper mapper;

    
    public MotelChainService(MotelChainRepository motelChainRepository, MotelChainMapper mapper) {
        this.motelChainRepository = motelChainRepository;
        this.mapper = mapper;
        
        // Proof that Spring creates a proxy! Uncomment to see:
        // System.out.println("Actual class: " + this.getClass().getName());
        // Output will be: MotelChainService$$SpringCGLIB$$0
    }

    /**
     * Get all motel chains with pagination.
     * Returns Page<MotelChain> for controller to convert to response DTOs.
     */
    public Page<MotelChain> getAllMotelChains(Pageable pageable) {
        return motelChainRepository.findAll(pageable);
    }

    /**
     * Get a single motel chain by ID.
     * Throws ResourceNotFoundException if not found (handled by GlobalExceptionHandler).
     */
    public MotelChain getMotelChainById(UUID motelChainId) {
        return motelChainRepository.findById(motelChainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MotelChain", "motelChainId", motelChainId));
    }

    /**
     * Create a new motel chain.
     * Checks for duplicates (name + state + pincode) and throws exception if exists.
     * 
     * @Transactional with readOnly=false for write operation
     */
    @Transactional
    public MotelChain createMotelChain(CreateMotelChainRequest request) {
        // Check for duplicate
        String motelChainName = request.getMotelChainName();
        String pincode = request.getPincode();
        String state = request.getState();
        
        if (motelChainRepository.existsByMotelChainNameAndPincodeAndState(
                motelChainName, pincode, state)) {
            throw new DuplicateResourceException(
                    "MotelChain", 
                    String.format("name='%s', state='%s', pincode='%s'", 
                            motelChainName, state, pincode));
        }
        
        // Map DTO to entity and save
        MotelChain entity = mapper.toEntity(request);
        return motelChainRepository.save(entity);
    }

    /**
     * Update an existing motel chain.
     * Throws ResourceNotFoundException if ID doesn't exist.
     */
    @Transactional
    public MotelChain updateMotelChain(UUID motelChainId, UpdateMotelChainRequest request) {
        // Fetch existing entity (throws exception if not found)
        MotelChain existingEntity = motelChainRepository.findById(motelChainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MotelChain", "motelChainId", motelChainId));
        
        // Update entity fields from request DTO
        mapper.updateEntity(request, existingEntity);
        
        // Save (JPA dirty checking will auto-update)
        return motelChainRepository.save(existingEntity);
    }

    /**
     * Soft delete a motel chain.
     * Throws ResourceNotFoundException if ID doesn't exist.
     * 
     * TODO: Implement actual soft delete (set deletedAt timestamp)
     * Currently does hard delete via deleteById()
     */
    @Transactional
    public void deleteMotelChain(UUID motelChainId) {
        if (!motelChainRepository.existsById(motelChainId)) {
            throw new ResourceNotFoundException(
                    "MotelChain", "motelChainId", motelChainId);
        }
        
        motelChainRepository.deleteById(motelChainId);
        
        // Future: Implement soft delete
        // MotelChain entity = getMotelChainById(motelChainId);
        // entity.setDeletedAt(LocalDateTime.now());
        // motelChainRepository.save(entity);
    }
}