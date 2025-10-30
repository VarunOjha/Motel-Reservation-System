package com.example.motels.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) Configuration for Motel Management API
 * 
 * ═══════════════════════════════════════════════════════════════════
 *                        OPENAPI/SWAGGER THEORY
 * ═══════════════════════════════════════════════════════════════════
 * 
 * What is OpenAPI?
 * ================
 * OpenAPI = Industry-standard specification for REST APIs
 * - Formerly known as "Swagger Specification"
 * - Language-agnostic way to describe REST APIs
 * - Machine-readable (JSON/YAML) and human-readable
 * 
 * What is Swagger?
 * ================
 * Swagger = Set of tools built around OpenAPI specification
 * - Swagger UI: Interactive API documentation
 * - Swagger Editor: Design APIs
 * - Swagger Codegen: Generate client SDKs
 * 
 * How SpringDoc Works:
 * ====================
 * 1. Scans Spring annotations at startup
 *    - @RestController, @RequestMapping, @GetMapping, etc.
 *    - @RequestBody, @PathVariable, @RequestParam
 *    - Bean Validation annotations (@NotNull, @Size, etc.)
 * 
 * 2. Generates OpenAPI specification (JSON)
 *    - Available at: /v3/api-docs
 *    - Contains all endpoints, schemas, validations
 * 
 * 3. Serves Swagger UI
 *    - Available at: /swagger-ui.html
 *    - Interactive interface to test APIs
 *    - Auto-populated from OpenAPI spec
 * 
 * ═══════════════════════════════════════════════════════════════════
 *                        CONFIGURATION BREAKDOWN
 * ═══════════════════════════════════════════════════════════════════
 * 
 * @Configuration
 * =============
 * - Marks class as Spring configuration
 * - Beans defined here are registered in Spring context
 * 
 * @Bean OpenAPI
 * ============
 * - Customizes the generated OpenAPI specification
 * - Without this: SpringDoc uses defaults
 * - With this: We can add metadata, servers, security, etc.
 * 
 * Info Object
 * ===========
 * - title: API name shown in Swagger UI
 * - version: API version (for versioning strategy)
 * - description: Markdown-supported description
 * - contact: Developer/team contact info
 * - license: API license information
 * 
 * Server Object
 * =============
 * - Defines base URLs for API
 * - Useful for multiple environments (dev, staging, prod)
 * - Swagger UI uses this for "Try it out" feature
 * 
 * ═══════════════════════════════════════════════════════════════════
 *                        BENEFITS
 * ═══════════════════════════════════════════════════════════════════
 * 
 * For Developers:
 * ===============
 * ✅ No manual documentation needed
 * ✅ Always up-to-date (generated from code)
 * ✅ Interactive testing in browser
 * ✅ See request/response schemas
 * ✅ Understand validation rules
 * 
 * For Frontend Teams:
 * ===================
 * ✅ Clear API contracts
 * ✅ Test endpoints without Postman
 * ✅ Generate TypeScript types from OpenAPI spec
 * ✅ Mock API responses for development
 * 
 * For QA Teams:
 * =============
 * ✅ Understand all endpoints
 * ✅ See expected inputs/outputs
 * ✅ Test edge cases interactively
 * ✅ Export OpenAPI spec for automated testing
 * 
 * For DevOps:
 * ===========
 * ✅ API gateway configuration
 * ✅ Rate limiting rules
 * ✅ Monitoring endpoint health
 * 
 * ═══════════════════════════════════════════════════════════════════
 *                        ACCESSING SWAGGER
 * ═══════════════════════════════════════════════════════════════════
 * 
 * After starting the application:
 * 
 * 1. Swagger UI (Interactive):
 *    http://localhost:8085/swagger-ui.html
 *    or
 *    http://localhost:8085/swagger-ui/index.html
 * 
 * 2. OpenAPI JSON Spec:
 *    http://localhost:8085/v3/api-docs
 * 
 * 3. OpenAPI YAML Spec:
 *    http://localhost:8085/v3/api-docs.yaml
 * 
 * ═══════════════════════════════════════════════════════════════════
 *                        ANNOTATIONS FOR ENHANCEMENT
 * ═══════════════════════════════════════════════════════════════════
 * 
 * You can enhance documentation with these annotations:
 * 
 * @Tag (on Controller):
 * --------------------
 * @Tag(name = "Motel Chain", description = "Motel Chain management APIs")
 * - Groups related endpoints
 * - Shows in Swagger UI sidebar
 * 
 * @Operation (on Method):
 * -----------------------
 * @Operation(summary = "Create new motel chain", 
 *            description = "Creates a new motel chain with validation")
 * - Describes what the endpoint does
 * - Shows in Swagger UI for each endpoint
 * 
 * @ApiResponse (on Method):
 * -------------------------
 * @ApiResponse(responseCode = "201", description = "Successfully created")
 * @ApiResponse(responseCode = "400", description = "Validation failed")
 * - Documents possible HTTP responses
 * - Shows status codes and meanings
 * 
 * @Schema (on DTO fields):
 * -----------------------
 * @Schema(description = "Unique identifier", example = "uuid-here")
 * - Describes DTO fields
 * - Provides examples for Swagger UI
 * 
 * @Parameter (on method parameters):
 * ----------------------------------
 * @Parameter(description = "Motel chain ID", required = true)
 * - Describes path/query parameters
 * - Shows if required or optional
 * 
 * ═══════════════════════════════════════════════════════════════════
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures OpenAPI specification metadata
     * 
     * THEORY: Why customize OpenAPI?
     * ==============================
     * Default SpringDoc config works, but lacks:
     * - Proper API title and description
     * - Version information
     * - Contact details
     * - Server URLs for different environments
     * 
     * This configuration provides:
     * - Professional API documentation
     * - Clear versioning strategy
     * - Contact information for API consumers
     * - Server URLs for testing
     * 
     * @return Customized OpenAPI object
     */
    @Bean
    public OpenAPI motelManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Motel Management API")
                        .description("""
                                RESTful API for managing motel chains, motels, rooms, and reservations.
                                
                                ## Features
                                - **DTOs with MapStruct**: Type-safe, compile-time bean mapping
                                - **Bean Validation**: JSR-380 validation on all inputs
                                - **Correlation ID**: Request tracking across all responses
                                - **Pagination**: Efficient data retrieval with Spring Data
                                - **Global Exception Handling**: Consistent error responses
                                
                                ## Response Format
                                All successful responses follow this structure:
                                ```json
                                {
                                  "status": "200",
                                  "correlationId": "uuid",
                                  "timestamp": "2025-10-30T...",
                                  "data": { ... }
                                }
                                ```
                                
                                All error responses follow this structure:
                                ```json
                                {
                                  "status": "404",
                                  "correlationId": "uuid",
                                  "timestamp": "2025-10-30T...",
                                  "message": "Error description",
                                  "path": "/api/endpoint"
                                }
                                ```
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Motel Management Team")
                                .email("support@motelmanagement.com")
                                .url("https://github.com/VarunOjha/Motel-Reservation-System"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8085")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.motelmanagement.com")
                                .description("Production Server (when deployed)")
                ));
    }
}
