package com.example.motels.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ContactInfo in API responses.
 * Contains all contact fields to be exposed to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfoResponse {
    private String phoneNumber;
    private String email;
    private String contactName;
    private String contactPosition;
    private String contactType;
    private String contactDescription;
    private String status;
}
