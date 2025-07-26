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

    Optional<Room> findByRoomIdAndMotelChainIdAndMotelId(UUID roomId, UUID motelChainId, UUID motelId);

    boolean existsByRoomIdAndMotelChainIdAndMotelId(UUID roomId, UUID motelChainId, UUID motelId);
}
