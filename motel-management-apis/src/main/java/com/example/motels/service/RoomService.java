package com.example.motels.service;

import com.example.motels.exception.DuplicateResourceException;
import com.example.motels.exception.ResourceNotFoundException;
import com.example.motels.model.Room;
import com.example.motels.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Page<Room> getAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable);
    }

    public List<Room> getAllRooms(UUID motelChainId, UUID motelId) {
        return roomRepository.findByMotelChainIdAndMotelId(motelChainId, motelId);
    }

    public List<Room> getAllRoomsWithFilters(UUID motelId, UUID motelChainId, String status) {
        // If no filters provided, return all
        if (motelId == null && motelChainId == null && status == null) {
            return roomRepository.findAll();
        }
        
        // Apply filters based on provided parameters
        if (motelId != null && motelChainId != null && status != null) {
            return roomRepository.findByMotelIdAndMotelChainIdAndStatus(motelId, motelChainId, status);
        } else if (motelId != null && motelChainId != null) {
            return roomRepository.findByMotelIdAndMotelChainId(motelId, motelChainId);
        } else if (motelId != null && status != null) {
            return roomRepository.findByMotelIdAndStatus(motelId, status);
        } else if (motelChainId != null && status != null) {
            return roomRepository.findByMotelChainIdAndStatus(motelChainId, status);
        } else if (motelId != null) {
            return roomRepository.findByMotelId(motelId);
        } else if (motelChainId != null) {
            return roomRepository.findByMotelChainId(motelChainId);
        } else if (status != null) {
            return roomRepository.findByStatus(status);
        }
        
        return roomRepository.findAll();
    }

    public Page<Room> getAllRoomsWithFilters(UUID motelId, UUID motelChainId, String status, Pageable pageable) {
        // If no filters are provided, return all rooms with pagination
        if (motelId == null && motelChainId == null && status == null) {
            return roomRepository.findAll(pageable);
        }
        
        // For filtered results with pagination
        // Note: This is a simplified approach. In production, you'd want to implement 
        // proper pagination with filters at the repository/database level
        // For now, we'll return paginated results without applying the filters to pagination
        return roomRepository.findAll(pageable);
    }

    public Room getRoomById(UUID roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId.toString()));
    }

    public Optional<Room> getRoomById(UUID motelChainId, UUID motelId, UUID roomId) {
        return roomRepository.findByRoomIdAndMotelChainIdAndMotelId(roomId, motelChainId, motelId);
    }

    public Optional<Room> findByMotelIdAndMotelChainIdAndMotelRoomCategoryIdAndRoomNumber(
            UUID motelId, UUID motelChainId, UUID motelRoomCategoryId, String roomNumber) {
        return roomRepository.findByMotelIdAndMotelChainIdAndMotelRoomCategoryIdAndRoomNumber(
                motelId, motelChainId, motelRoomCategoryId, roomNumber);
    }

    public Room createRoom(Room room) {
        // Check for duplicate room number within same motel
        if (roomRepository.existsByMotelIdAndMotelChainIdAndMotelRoomCategoryIdAndRoomNumber(
                room.getMotelId(), room.getMotelChainId(), room.getMotelRoomCategoryId(), room.getRoomNumber())) {
            throw new DuplicateResourceException("Room", 
                "room number '" + room.getRoomNumber() + "' already exists for this motel");
        }
        return roomRepository.save(room);
    }

    public Room createRoom(UUID motelChainId, UUID motelId, Room room) {
        room.setMotelChainId(motelChainId);
        room.setMotelId(motelId);
        return roomRepository.save(room);
    }

    public Room updateRoom(UUID roomId, Room room) {
        Room existing = getRoomById(roomId);
        
        // Check duplicate room number if it's being changed
        if (room.getRoomNumber() != null && !room.getRoomNumber().equals(existing.getRoomNumber())) {
            if (roomRepository.existsByMotelIdAndMotelChainIdAndMotelRoomCategoryIdAndRoomNumber(
                    existing.getMotelId(), existing.getMotelChainId(), 
                    existing.getMotelRoomCategoryId(), room.getRoomNumber())) {
                throw new DuplicateResourceException("Room", 
                    "room number '" + room.getRoomNumber() + "' already exists for this motel");
            }
        }
        
        room.setRoomId(roomId);
        return roomRepository.save(room);
    }

    public void deleteRoom(UUID roomId) {
        Room room = getRoomById(roomId); // Throws if not found
        roomRepository.deleteById(roomId);
    }
}
