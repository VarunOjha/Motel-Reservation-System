package com.example.motels.repository;

import com.example.motels.model.RoomCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomCategoryRepository extends JpaRepository<RoomCategory, UUID> {

    List<RoomCategory> findByMotelChainIdAndMotelId(UUID motelChainId, UUID motelId);
    List<RoomCategory> findByMotelId(UUID motelId);
    List<RoomCategory> findByMotelChainId(UUID motelChainId);
    List<RoomCategory> findByStatus(String status);
    List<RoomCategory> findByMotelIdAndMotelChainId(UUID motelId, UUID motelChainId);
    List<RoomCategory> findByMotelIdAndStatus(UUID motelId, String status);
    List<RoomCategory> findByMotelChainIdAndStatus(UUID motelChainId, String status);
    List<RoomCategory> findByMotelIdAndMotelChainIdAndStatus(UUID motelId, UUID motelChainId, String status);

    Optional<RoomCategory> findByMotelRoomCategoryIdAndMotelChainIdAndMotelId(UUID roomCategoryId, UUID motelChainId, UUID motelId);

    boolean existsByMotelRoomCategoryIdAndMotelChainIdAndMotelId(UUID roomCategoryId, UUID motelChainId, UUID motelId);
    boolean existsByRoomCategoryNameAndMotelChainIdAndMotelId(String roomCategoryName, UUID motelChainId, UUID motelId);
    RoomCategory findByRoomCategoryNameAndMotelChainIdAndMotelId(String roomCategoryName, UUID motelChainId, UUID motelId);
}
