package com.example.motels.service;

import com.example.motels.model.MotelChain;
import com.example.motels.repository.MotelChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for MotelChainService using mocks
 * These tests focus on service behavior without database dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MotelChainService Integration Tests")
class MotelChainServiceIntegrationTest {

    @Mock
    private MotelChainRepository motelChainRepository;

    private MotelChainService motelChainService;

    private MotelChain testMotelChain;

    @BeforeEach
    void setUp() {
        motelChainService = new MotelChainService(motelChainRepository);
        
        testMotelChain = new MotelChain();
        testMotelChain.setMotelChainName("Integration Test Chain");
        testMotelChain.setDisplayName("Test Display Name");
        testMotelChain.setState("California");
        testMotelChain.setPincode("90210");
        testMotelChain.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should create and retrieve motel chain successfully")
    void shouldCreateAndRetrieveMotelChainSuccessfully() {
        // Given
        UUID chainId = UUID.randomUUID();
        testMotelChain.setMotelChainId(chainId);
        
        when(motelChainRepository.existsByMotelChainNameAndPincodeAndState(
            testMotelChain.getMotelChainName(),
            testMotelChain.getPincode(),
            testMotelChain.getState()
        )).thenReturn(false);
        
        when(motelChainRepository.save(any(MotelChain.class))).thenReturn(testMotelChain);
        when(motelChainRepository.findById(chainId)).thenReturn(Optional.of(testMotelChain));
        
        // When
        MotelChain savedChain = motelChainService.createMotelChain(testMotelChain);
        Optional<MotelChain> retrievedChain = motelChainService.getMotelChainById(chainId);
        
        // Then
        assertThat(savedChain).isNotNull();
        assertThat(savedChain.getMotelChainName()).isEqualTo("Integration Test Chain");
        assertThat(retrievedChain).isPresent();
        assertThat(retrievedChain.get().getMotelChainName()).isEqualTo("Integration Test Chain");
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given
        List<MotelChain> chains = Arrays.asList(testMotelChain);
        Page<MotelChain> page = new PageImpl<>(chains, PageRequest.of(0, 10), 1);
        
        when(motelChainRepository.findAll(any(Pageable.class))).thenReturn(page);
        
        // When
        Page<MotelChain> result = motelChainService.getAllMotelChains(PageRequest.of(0, 10));
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testMotelChain);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should delete motel chain successfully")
    void shouldDeleteMotelChainSuccessfully() {
        // Given
        UUID chainId = UUID.randomUUID();
        when(motelChainRepository.existsById(chainId)).thenReturn(true);
        
        // When
        boolean result = motelChainService.deleteMotelChain(chainId);
        
        // Then
        assertThat(result).isTrue();
        verify(motelChainRepository).deleteById(chainId);
    }
}
