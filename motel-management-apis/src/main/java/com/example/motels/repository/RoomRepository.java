package com.example.motels.repository;

import com.example.motels.model.Room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByMotelChainIdAndMotelId(UUID motelChainId, UUID motelId);
    List<Room> findByMotelId(UUID motelId);
    List<Room> findByMotelChainId(UUID motelChainId);
    List<Room> findByStatus(String status);
    List<Room> findByMotelIdAndMotelChainId(UUID motelId, UUID motelChainId);
    List<Room> findByMotelIdAndStatus(UUID motelId, String status);
    List<Room> findByMotelChainIdAndStatus(UUID motelChainId, String status);
    List<Room> findByMotelIdAndMotelChainIdAndStatus(UUID motelId, UUID motelChainId, String status);

    Optional<Room> findByRoomIdAndMotelChainIdAndMotelId(UUID roomId, UUID motelChainId, UUID motelId);

    boolean existsByRoomIdAndMotelChainIdAndMotelId(UUID roomId, UUID motelChainId, UUID motelId);
}
