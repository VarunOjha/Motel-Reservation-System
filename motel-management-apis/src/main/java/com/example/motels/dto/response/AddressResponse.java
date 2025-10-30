package com.example.motels.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Address in API responses.
 * Contains all address fields to be exposed to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String addressName;
    private String status;
}
