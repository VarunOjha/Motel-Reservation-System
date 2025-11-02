package com.example.motels.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Contact information")
public class ContactInfoRequest {
    
    @Schema(description = "Phone number (10-15 digits)", example = "+14155551234", required = true)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be 10-15 digits, optionally starting with +")
    private String phoneNumber;
    
    @Schema(description = "Email address", example = "contact@hilton.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Schema(description = "Name of the contact person", example = "John Smith", required = true)
    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    private String contactName;
    
    @Schema(description = "Position/title of the contact", example = "General Manager", required = false)
    @Size(max = 100, message = "Contact position must not exceed 100 characters")
    private String contactPosition;
    
    @Schema(description = "Type of contact", example = "PRIMARY", required = true)
    @NotBlank(message = "Contact type is required")
    @Size(max = 50, message = "Contact type must not exceed 50 characters")
    private String contactType;
    
    @Schema(description = "Additional contact details", example = "Available 24/7", required = false)
    @Size(max = 500, message = "Contact description must not exceed 500 characters")
    private String contactDescription;
    
    @Schema(description = "Status of the contact", example = "ACTIVE", required = true)
    @NotBlank(message = "Contact status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;
}
