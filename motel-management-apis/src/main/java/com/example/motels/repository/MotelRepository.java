package com.example.motels.repository;

import com.example.motels.model.Motel;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotelRepository extends JpaRepository<Motel, UUID> {
    boolean existsByMotelChainIdAndMotelNameAndPincodeAndState(UUID motelChainId, String motelName, String pincode, String state);
    Motel getByMotelChainIdAndMotelNameAndPincodeAndState(UUID motelChainId, String motelName, String pincode, String state);
    List<Motel> getByMotelChainId(UUID motelChainId);
}
