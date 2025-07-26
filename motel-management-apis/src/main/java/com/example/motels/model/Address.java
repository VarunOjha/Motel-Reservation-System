package com.example.motels.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
public class Address {
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String addressName;
    private String status;
}