package com.example.motels.repository;

import com.example.motels.model.MotelChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MotelChainRepository extends JpaRepository<MotelChain, UUID> {
}