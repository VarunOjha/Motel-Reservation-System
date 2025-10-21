package com.example.motels.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ContactInfo input in requests.
 * Nested in MotelChain create/update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfoRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be 10-15 digits, optionally starting with +")
    private String phoneNumber;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    private String contactName;
    
    @Size(max = 100, message = "Contact position must not exceed 100 characters")
    private String contactPosition;
    
    @NotBlank(message = "Contact type is required")
    @Size(max = 50, message = "Contact type must not exceed 50 characters")
    private String contactType;
    
    @Size(max = 500, message = "Contact description must not exceed 500 characters")
    private String contactDescription;
    
    @NotBlank(message = "Contact status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;
}
