package com.example.motels.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;


@Data
@Entity
public class MotelChain {

    @Id
    @GeneratedValue
    private UUID motelChainId;

    private String motelChainName;
    private String displayName;
    private String state;
    private String pincode;
    private String status;

    @JdbcTypeCode(SqlTypes.JSON) 
    @Column(columnDefinition = "jsonb")
    private MotelChainAddress address;

    @JdbcTypeCode(SqlTypes.JSON) 
    @Column(columnDefinition = "jsonb")
    private MotelChainContactInfo contactInfo;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}