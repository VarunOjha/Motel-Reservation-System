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

    Optional<RoomCategory> findByIdAndMotelChainIdAndMotelId(UUID roomCategoryId, UUID motelChainId, UUID motelId);

    boolean existsByIdAndMotelChainIdAndMotelId(UUID roomCategoryId, UUID motelChainId, UUID motelId);
}
