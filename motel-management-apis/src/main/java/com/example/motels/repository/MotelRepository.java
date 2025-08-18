package com.example.motels.repository;

import com.example.motels.model.Motel;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MotelRepository extends JpaRepository<Motel, UUID> {
    
    @Query("SELECT m FROM Motel m WHERE " +
           "(:motelId IS NULL OR m.motelId = :motelId) OR " +
           "(:motelChainId IS NULL OR m.motelChainId = :motelChainId) OR " +
           "(:status IS NULL OR m.status = :status) OR " +
           "(:pincode IS NULL OR m.pincode = :pincode) OR " +
           "(:state IS NULL OR m.state = :state)")
    List<Motel> findMotelsWithFilters(
            @Param("motelId") UUID motelId,
            @Param("motelChainId") String motelChainId,
            @Param("status") String status,
            @Param("pincode") String pincode,
            @Param("state") String state);

    Optional<Motel> findByMotelChainIdAndMotelNameAndPincodeAndState(String motelChainId, String motelName, String pincode,
              String state);
}
