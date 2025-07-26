package com.example.motels.service;

import com.example.motels.model.RoomCategory;
import com.example.motels.repository.RoomCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomCategoryService {

    @Autowired
    private RoomCategoryRepository roomCategoryRepository;

    public List<RoomCategory> getAllRoomCategories(UUID motelChainId, UUID motelId) {
        return roomCategoryRepository.findByMotelChainIdAndMotelId(motelChainId, motelId);
    }

    public Optional<RoomCategory> getRoomCategoryById(UUID motelChainId, UUID motelId, UUID roomCategoryId) {
        return roomCategoryRepository.findByIdAndMotelChainIdAndMotelId(roomCategoryId, motelChainId, motelId);
    }

    public RoomCategory createRoomCategory(UUID motelChainId, UUID motelId, RoomCategory roomCategory) {
        roomCategory.setMotelChainId(motelChainId);
        roomCategory.setMotelId(motelId);
        return roomCategoryRepository.save(roomCategory);
    }

    public RoomCategory updateRoomCategory(UUID motelChainId, UUID motelId, UUID roomCategoryId, RoomCategory roomCategory) {
        if (roomCategoryRepository.existsByIdAndMotelChainIdAndMotelId(roomCategoryId, motelChainId, motelId)) {
            roomCategory.setMotelRoomCategoryId(roomCategoryId);
            roomCategory.setMotelChainId(motelChainId);
            roomCategory.setMotelId(motelId);
            return roomCategoryRepository.save(roomCategory);
        }
        return null;
    }

    public boolean deleteRoomCategory(UUID motelChainId, UUID motelId, UUID roomCategoryId) {
        if (roomCategoryRepository.existsByIdAndMotelChainIdAndMotelId(roomCategoryId, motelChainId, motelId)) {
            roomCategoryRepository.deleteById(roomCategoryId);
            return true;
        }
        return false;
    }
}
