package com.example.motels.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for AllMotels count statistics.
 * Provides counts of all entities in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing entity count statistics")
public class AllMotelsCountResponse {

    @Schema(description = "Counts for each PostgreSQL table")
    private Map<String, Long> postgresqlTables;

    @Schema(description = "Total number of records across all PostgreSQL tables", example = "150")
    private Long totalPostgresqlRecords;

    @Schema(description = "Additional information", example = "MongoDB collections (prices, reservations) are managed by the Go reservation-apis service")
    private String note;
}
