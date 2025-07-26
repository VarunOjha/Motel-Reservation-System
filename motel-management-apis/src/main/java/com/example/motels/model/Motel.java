package com.example.motels.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Motel {
    private UUID motelChainId;
    
    @Id
    @GeneratedValue
    private UUID motelId;
    private String motelName;
    private String displayName;
    private String status;
    private String pincode;
    private String state;

    @JdbcTypeCode(SqlTypes.JSON) 
    @Column(columnDefinition = "jsonb")
    private Address address;

    @JdbcTypeCode(SqlTypes.JSON) 
    @Column(columnDefinition = "jsonb")
    private ContactInfo contactInfo;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
