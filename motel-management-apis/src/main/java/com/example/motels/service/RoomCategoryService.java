package com.example.motels.service;

import com.example.motels.model.RoomCategory;
import com.example.motels.repository.RoomCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomCategoryService {

    @Autowired
    private RoomCategoryRepository roomCategoryRepository;

    public List<RoomCategory> getAllRoomCategories() {
        return roomCategoryRepository.findAll();
    }

    public List<RoomCategory> getAllRoomCategories(UUID motelChainId, UUID motelId) {
        return roomCategoryRepository.findByMotelChainIdAndMotelId(motelChainId, motelId);
    }

    public List<RoomCategory> getAllRoomCategoriesWithFilters(UUID motelId, UUID motelChainId, String status) {
        // If no filters provided, return all
        if (motelId == null && motelChainId == null && status == null) {
            return roomCategoryRepository.findAll();
        }
        
        // Apply filters based on provided parameters
        if (motelId != null && motelChainId != null && status != null) {
            return roomCategoryRepository.findByMotelIdAndMotelChainIdAndStatus(motelId, motelChainId, status);
        } else if (motelId != null && motelChainId != null) {
            return roomCategoryRepository.findByMotelIdAndMotelChainId(motelId, motelChainId);
        } else if (motelId != null && status != null) {
            return roomCategoryRepository.findByMotelIdAndStatus(motelId, status);
        } else if (motelChainId != null && status != null) {
            return roomCategoryRepository.findByMotelChainIdAndStatus(motelChainId, status);
        } else if (motelId != null) {
            return roomCategoryRepository.findByMotelId(motelId);
        } else if (motelChainId != null) {
            return roomCategoryRepository.findByMotelChainId(motelChainId);
        } else if (status != null) {
            return roomCategoryRepository.findByStatus(status);
        }
        
        return roomCategoryRepository.findAll();
    }

    public Page<RoomCategory> getAllRoomCategoriesWithFilters(UUID motelId, UUID motelChainId, String status, Pageable pageable) {
        // If no filters provided, return all with pagination
        if (motelId == null && motelChainId == null && status == null) {
            return roomCategoryRepository.findAll(pageable);
        }
        
        // Apply filters based on provided parameters with pagination
        if (motelId != null && motelChainId != null && status != null) {
            return roomCategoryRepository.findByMotelIdAndMotelChainIdAndStatus(motelId, motelChainId, status, pageable);
        } else if (motelId != null && motelChainId != null) {
            return roomCategoryRepository.findByMotelIdAndMotelChainId(motelId, motelChainId, pageable);
        } else if (motelId != null && status != null) {
            return roomCategoryRepository.findByMotelIdAndStatus(motelId, status, pageable);
        } else if (motelChainId != null && status != null) {
            return roomCategoryRepository.findByMotelChainIdAndStatus(motelChainId, status, pageable);
        } else if (motelId != null) {
            return roomCategoryRepository.findByMotelId(motelId, pageable);
        } else if (motelChainId != null) {
            return roomCategoryRepository.findByMotelChainId(motelChainId, pageable);
        } else if (status != null) {
            return roomCategoryRepository.findByStatus(status, pageable);
        }
        
        return roomCategoryRepository.findAll(pageable);
    }

    public Optional<RoomCategory> getRoomCategoryById(UUID roomCategoryId) {
        return roomCategoryRepository.findById(roomCategoryId);
    }

    public Optional<RoomCategory> getRoomCategoryById(UUID motelChainId, UUID motelId, UUID roomCategoryId) {
        return roomCategoryRepository.findByMotelRoomCategoryIdAndMotelChainIdAndMotelId(roomCategoryId, motelChainId, motelId);
    }

    public RoomCategory createRoomCategory(RoomCategory roomCategory) {
        String roomCategoryName = roomCategory.getRoomCategoryName();
        if (roomCategoryRepository.existsByRoomCategoryNameAndMotelChainIdAndMotelId(roomCategoryName, roomCategory.getMotelChainId(), roomCategory.getMotelId())) {
            return roomCategoryRepository.findByRoomCategoryNameAndMotelChainIdAndMotelId(roomCategoryName, roomCategory.getMotelChainId(), roomCategory.getMotelId());
        }
        return roomCategoryRepository.save(roomCategory);
    }

    public RoomCategory createRoomCategory(UUID motelChainId, UUID motelId, RoomCategory roomCategory) {
        roomCategory.setMotelChainId(motelChainId);
        roomCategory.setMotelId(motelId);
        String roomCategoryName = roomCategory.getRoomCategoryName();
        if (roomCategoryRepository.existsByRoomCategoryNameAndMotelChainIdAndMotelId(roomCategoryName, motelChainId, motelId)) {
            return roomCategoryRepository.findByRoomCategoryNameAndMotelChainIdAndMotelId(roomCategoryName, motelChainId, motelId);
        }
        return roomCategoryRepository.save(roomCategory);
    }

    public RoomCategory updateRoomCategory(UUID roomCategoryId, RoomCategory roomCategory) {
        if (roomCategoryRepository.existsByMotelRoomCategoryIdAndMotelChainIdAndMotelId(roomCategoryId, roomCategory.getMotelChainId(), roomCategory.getMotelId())) {
            roomCategory.setMotelRoomCategoryId(roomCategoryId);
            return roomCategoryRepository.save(roomCategory);
        }
        return null;
    }

    public RoomCategory updateRoomCategory(UUID motelChainId, UUID motelId, UUID roomCategoryId, RoomCategory roomCategory) {
        if (roomCategoryRepository.existsByMotelRoomCategoryIdAndMotelChainIdAndMotelId(roomCategoryId, motelChainId, motelId)) {
            roomCategory.setMotelRoomCategoryId(roomCategoryId);
            roomCategory.setMotelChainId(motelChainId);
            roomCategory.setMotelId(motelId);
            return roomCategoryRepository.save(roomCategory);
        }
        return null;
    }

    public boolean deleteRoomCategory(UUID motelChainId, UUID motelId, UUID roomCategoryId) {
        if (roomCategoryRepository.existsByMotelRoomCategoryIdAndMotelChainIdAndMotelId(roomCategoryId, motelChainId, motelId)) {
            roomCategoryRepository.deleteById(roomCategoryId);
            return true;
        }
        return false;
    }
}
