package com.example.motels.service;


import com.example.motels.model.MotelChain;
import com.example.motels.repository.MotelChainRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MotelChainService {

    private final MotelChainRepository motelChainRepository;

    
    public MotelChainService(MotelChainRepository motelChainRepository) {
        this.motelChainRepository = motelChainRepository;
    }

    public List<MotelChain> getAllMotelChains() {
        return motelChainRepository.findAll();
    }

    public Optional<MotelChain> getMotelChainById(UUID motelChainId) {
        return motelChainRepository.findById(motelChainId);
    }

    public MotelChain createMotelChain(MotelChain motelChain) {
        return motelChainRepository.save(motelChain);
    }

    public MotelChain updateMotelChain(UUID motelChainId, MotelChain motelChainDetails) {
        MotelChain motelChain = motelChainRepository.findById(motelChainId)
                .orElseThrow(() -> new RuntimeException("MotelChain not found"));
        motelChain.setMotelChainName(motelChainDetails.getMotelChainName());
        motelChain.setDisplayName(motelChainDetails.getDisplayName());
        motelChain.setState(motelChainDetails.getState());
        motelChain.setPincode(motelChainDetails.getPincode());
        motelChain.setStatus(motelChainDetails.getStatus());

        // Update newly added properties
        motelChain.setAddress(motelChainDetails.getAddress());
        motelChain.setContactInfo(motelChainDetails.getContactInfo());
        
        return motelChainRepository.save(motelChain);
    }

    public Boolean deleteMotelChain(UUID motelChainId) {
        if (!motelChainRepository.existsById(motelChainId)) {
            return false;
        }
        motelChainRepository.deleteById(motelChainId);
        return true;
    }
}