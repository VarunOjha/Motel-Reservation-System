package com.example.motels.service;

import com.example.motels.model.Room;
import com.example.motels.repository.RoomRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms(UUID motelChainId, UUID motelId) {
        return roomRepository.findByMotelChainIdAndMotelId(motelChainId, motelId);
    }

    public Optional<Room> getRoomById(UUID motelChainId, UUID motelId, UUID roomId) {
        return roomRepository.findByRoomIdAndMotelChainIdAndMotelId(roomId, motelChainId, motelId);
    }

    public Room createRoom(UUID motelChainId, UUID motelId, Room room) {
        room.setMotelChainId(motelChainId);
        room.setMotelId(motelId);
        return roomRepository.save(room);
    }

    public Room updateRoom(UUID motelChainId, UUID motelId, UUID roomId, Room room) {
        if (roomRepository.existsByRoomIdAndMotelChainIdAndMotelId(roomId, motelChainId, motelId)) {
            room.setRoomId(roomId);
            room.setMotelChainId(motelChainId);
            room.setMotelId(motelId);
            return roomRepository.save(room);
        }
        return null;
    }

    public boolean deleteRoom(UUID motelChainId, UUID motelId, UUID roomId) {
        if (roomRepository.existsByRoomIdAndMotelChainIdAndMotelId(roomId, motelChainId, motelId)) {
            roomRepository.deleteById(roomId);
            return true;
        }
        return false;
    }
}
