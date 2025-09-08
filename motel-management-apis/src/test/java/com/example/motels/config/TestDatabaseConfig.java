package com.example.motels.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for H2 database compatibility
 * Handles PostgreSQL-specific features for testing environment
 */
@TestConfiguration
@Profile("test")
public class TestDatabaseConfig {
    // This class serves as a marker for test-specific configuration
    // The actual H2 compatibility is handled via application-test.properties
}
