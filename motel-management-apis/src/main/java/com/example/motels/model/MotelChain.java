package com.example.motels.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import lombok.*;
import java.util.UUID;


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

}