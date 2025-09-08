package com.example.motels.service;

import com.example.motels.model.Address;
import com.example.motels.model.ContactInfo;
import com.example.motels.model.MotelChain;
import com.example.motels.repository.MotelChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MotelChainService Tests")
class MotelChainServiceTest {

    @Mock
    private MotelChainRepository motelChainRepository;

    @InjectMocks
    private MotelChainService motelChainService;

    private MotelChain testMotelChain;
    private UUID testId;
    private Address testAddress;
    private ContactInfo testContactInfo;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        testAddress = new Address();
        testAddress.setAddressLine1("123 Main St");
        testAddress.setAddressLine2("Suite 100");
        testAddress.setLandmark("Near Central Park");
        testAddress.setAddressName("Main Office");
        testAddress.setStatus("ACTIVE");
        
        testContactInfo = new ContactInfo();
        testContactInfo.setPhoneNumber("123-456-7890");
        testContactInfo.setEmail("test@example.com");
        testContactInfo.setContactName("John Doe");
        testContactInfo.setContactPosition("Manager");
        testContactInfo.setContactType("PRIMARY");
        testContactInfo.setContactDescription("Main contact for operations");
        testContactInfo.setStatus("ACTIVE");
        
        testMotelChain = new MotelChain();
        testMotelChain.setMotelChainId(testId);
        testMotelChain.setMotelChainName("Test Motel Chain");
        testMotelChain.setDisplayName("Test Display Name");
        testMotelChain.setState("California");
        testMotelChain.setPincode("90210");
        testMotelChain.setStatus("ACTIVE");
        testMotelChain.setAddress(testAddress);
        testMotelChain.setContactInfo(testContactInfo);
        testMotelChain.setCreatedAt(LocalDateTime.now());
        testMotelChain.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Get All Motel Chains Tests")
    class GetAllMotelChainsTests {

        @Test
        @DisplayName("Should return all motel chains when repository contains data")
        void shouldReturnAllMotelChains() {
            // Given
            List<MotelChain> expectedChains = Arrays.asList(testMotelChain, createAnotherMotelChain());
            when(motelChainRepository.findAll()).thenReturn(expectedChains);

            // When
            List<MotelChain> actualChains = motelChainService.getAllMotelChains();

            // Then
            assertThat(actualChains).hasSize(2);
            assertThat(actualChains).containsExactlyElementsOf(expectedChains);
            verify(motelChainRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no motel chains exist")
        void shouldReturnEmptyListWhenNoMotelChainsExist() {
            // Given
            when(motelChainRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<MotelChain> actualChains = motelChainService.getAllMotelChains();

            // Then
            assertThat(actualChains).isEmpty();
            verify(motelChainRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return paginated motel chains")
        void shouldReturnPaginatedMotelChains() {
            // Given
            Pageable pageable = PageRequest.of(0, 2);
            List<MotelChain> chains = Arrays.asList(testMotelChain, createAnotherMotelChain());
            Page<MotelChain> expectedPage = new PageImpl<>(chains, pageable, 2);
            when(motelChainRepository.findAll(pageable)).thenReturn(expectedPage);

            // When
            Page<MotelChain> actualPage = motelChainService.getAllMotelChains(pageable);

            // Then
            assertThat(actualPage.getContent()).hasSize(2);
            assertThat(actualPage.getTotalElements()).isEqualTo(2);
            assertThat(actualPage.getNumber()).isEqualTo(0);
            verify(motelChainRepository, times(1)).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Get Motel Chain By ID Tests")
    class GetMotelChainByIdTests {

        @Test
        @DisplayName("Should return motel chain when ID exists")
        void shouldReturnMotelChainWhenIdExists() {
            // Given
            when(motelChainRepository.findById(testId)).thenReturn(Optional.of(testMotelChain));

            // When
            Optional<MotelChain> result = motelChainService.getMotelChainById(testId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testMotelChain);
            verify(motelChainRepository, times(1)).findById(testId);
        }

        @Test
        @DisplayName("Should return empty when ID does not exist")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(motelChainRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When
            Optional<MotelChain> result = motelChainService.getMotelChainById(nonExistentId);

            // Then
            assertThat(result).isEmpty();
            verify(motelChainRepository, times(1)).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Create Motel Chain Tests")
    class CreateMotelChainTests {

        @Test
        @DisplayName("Should create new motel chain when unique combination")
        void shouldCreateNewMotelChainWhenUniqueCombination() {
            // Given
            when(motelChainRepository.existsByMotelChainNameAndPincodeAndState(
                    testMotelChain.getMotelChainName(),
                    testMotelChain.getPincode(),
                    testMotelChain.getState()
            )).thenReturn(false);
            when(motelChainRepository.save(testMotelChain)).thenReturn(testMotelChain);

            // When
            MotelChain result = motelChainService.createMotelChain(testMotelChain);

            // Then
            assertThat(result).isEqualTo(testMotelChain);
            verify(motelChainRepository, times(1)).existsByMotelChainNameAndPincodeAndState(
                    testMotelChain.getMotelChainName(),
                    testMotelChain.getPincode(),
                    testMotelChain.getState()
            );
            verify(motelChainRepository, times(1)).save(testMotelChain);
            verify(motelChainRepository, never()).getByMotelChainNameAndPincodeAndState(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return existing motel chain when duplicate combination")
        void shouldReturnExistingMotelChainWhenDuplicateCombination() {
            // Given
            MotelChain existingChain = createAnotherMotelChain();
            when(motelChainRepository.existsByMotelChainNameAndPincodeAndState(
                    testMotelChain.getMotelChainName(),
                    testMotelChain.getPincode(),
                    testMotelChain.getState()
            )).thenReturn(true);
            when(motelChainRepository.getByMotelChainNameAndPincodeAndState(
                    testMotelChain.getMotelChainName(),
                    testMotelChain.getPincode(),
                    testMotelChain.getState()
            )).thenReturn(existingChain);

            // When
            MotelChain result = motelChainService.createMotelChain(testMotelChain);

            // Then
            assertThat(result).isEqualTo(existingChain);
            verify(motelChainRepository, times(1)).existsByMotelChainNameAndPincodeAndState(
                    testMotelChain.getMotelChainName(),
                    testMotelChain.getPincode(),
                    testMotelChain.getState()
            );
            verify(motelChainRepository, never()).save(any(MotelChain.class));
            verify(motelChainRepository, times(1)).getByMotelChainNameAndPincodeAndState(
                    testMotelChain.getMotelChainName(),
                    testMotelChain.getPincode(),
                    testMotelChain.getState()
            );
        }
    }

    @Nested
    @DisplayName("Update Motel Chain Tests")
    class UpdateMotelChainTests {

        @Test
        @DisplayName("Should update existing motel chain successfully")
        void shouldUpdateExistingMotelChainSuccessfully() {
            // Given
            MotelChain updatedDetails = new MotelChain();
            updatedDetails.setMotelChainName("Updated Chain Name");
            updatedDetails.setDisplayName("Updated Display Name");
            updatedDetails.setState("Updated State");
            updatedDetails.setPincode("54321");
            updatedDetails.setStatus("INACTIVE");
            updatedDetails.setAddress(testAddress);
            updatedDetails.setContactInfo(testContactInfo);

            when(motelChainRepository.findById(testId)).thenReturn(Optional.of(testMotelChain));
            when(motelChainRepository.save(any(MotelChain.class))).thenReturn(testMotelChain);

            // When
            MotelChain result = motelChainService.updateMotelChain(testId, updatedDetails);

            // Then
            assertThat(result.getMotelChainName()).isEqualTo("Updated Chain Name");
            assertThat(result.getDisplayName()).isEqualTo("Updated Display Name");
            assertThat(result.getState()).isEqualTo("Updated State");
            assertThat(result.getPincode()).isEqualTo("54321");
            assertThat(result.getStatus()).isEqualTo("INACTIVE");
            verify(motelChainRepository, times(1)).findById(testId);
            verify(motelChainRepository, times(1)).save(testMotelChain);
        }

        @Test
        @DisplayName("Should throw exception when motel chain not found for update")
        void shouldThrowExceptionWhenMotelChainNotFoundForUpdate() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            MotelChain updatedDetails = new MotelChain();
            when(motelChainRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> motelChainService.updateMotelChain(nonExistentId, updatedDetails))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("MotelChain not found");

            verify(motelChainRepository, times(1)).findById(nonExistentId);
            verify(motelChainRepository, never()).save(any(MotelChain.class));
        }
    }

    @Nested
    @DisplayName("Delete Motel Chain Tests")
    class DeleteMotelChainTests {

        @Test
        @DisplayName("Should delete existing motel chain successfully")
        void shouldDeleteExistingMotelChainSuccessfully() {
            // Given
            when(motelChainRepository.existsById(testId)).thenReturn(true);
            doNothing().when(motelChainRepository).deleteById(testId);

            // When
            Boolean result = motelChainService.deleteMotelChain(testId);

            // Then
            assertThat(result).isTrue();
            verify(motelChainRepository, times(1)).existsById(testId);
            verify(motelChainRepository, times(1)).deleteById(testId);
        }

        @Test
        @DisplayName("Should return false when motel chain does not exist for deletion")
        void shouldReturnFalseWhenMotelChainDoesNotExistForDeletion() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(motelChainRepository.existsById(nonExistentId)).thenReturn(false);

            // When
            Boolean result = motelChainService.deleteMotelChain(nonExistentId);

            // Then
            assertThat(result).isFalse();
            verify(motelChainRepository, times(1)).existsById(nonExistentId);
            verify(motelChainRepository, never()).deleteById(any(UUID.class));
        }
    }

    private MotelChain createAnotherMotelChain() {
        UUID anotherId = UUID.randomUUID();
        
        Address anotherAddress = new Address();
        anotherAddress.setAddressLine1("456 Oak Ave");
        anotherAddress.setAddressLine2("Floor 2");
        anotherAddress.setLandmark("Near Shopping Mall");
        anotherAddress.setAddressName("Branch Office");
        anotherAddress.setStatus("ACTIVE");
        
        ContactInfo anotherContactInfo = new ContactInfo();
        anotherContactInfo.setPhoneNumber("987-654-3210");
        anotherContactInfo.setEmail("another@example.com");
        anotherContactInfo.setContactName("Jane Smith");
        anotherContactInfo.setContactPosition("Director");
        anotherContactInfo.setContactType("SECONDARY");
        anotherContactInfo.setContactDescription("Branch operations contact");
        anotherContactInfo.setStatus("ACTIVE");
        
        MotelChain anotherChain = new MotelChain();
        anotherChain.setMotelChainId(anotherId);
        anotherChain.setMotelChainName("Another Motel Chain");
        anotherChain.setDisplayName("Another Display Name");
        anotherChain.setState("New York");
        anotherChain.setPincode("10001");
        anotherChain.setStatus("ACTIVE");
        anotherChain.setAddress(anotherAddress);
        anotherChain.setContactInfo(anotherContactInfo);
        anotherChain.setCreatedAt(LocalDateTime.now());
        anotherChain.setUpdatedAt(LocalDateTime.now());
        
        return anotherChain;
    }
}
