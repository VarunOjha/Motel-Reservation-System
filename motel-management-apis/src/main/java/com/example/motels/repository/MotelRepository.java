package com.example.motels.repository;

import com.example.motels.model.Motel;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotelRepository extends JpaRepository<Motel, UUID> {
    // Additional query methods can be defined here if needed
}
