package com.example.motels.service;

import com.example.motels.model.Motel;
import com.example.motels.repository.MotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MotelService {

    @Autowired
    private MotelRepository motelRepository;

    public List<Motel> getAllMotels() {
        return motelRepository.findAll();
    }

    public List<Motel> getAllMotelsByChainId(String motelChainId) {
        // Example query logic, assuming a method exists in the repository
        return motelRepository.findAll(); // Replace with a custom query if needed
    }

    public Optional<Motel> getMotelById(UUID motelId) {
        return motelRepository.findById(motelId);
    }

    public Motel createMotel(Motel motel) {
        return motelRepository.save(motel);
    }

    public Motel updateMotel(Motel motel) {
        return motelRepository.save(motel);
    }

    public void deleteMotel(UUID motelId) {
        motelRepository.deleteById(motelId);
    }
}
