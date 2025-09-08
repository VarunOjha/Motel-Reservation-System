# MotelChainService Test Suite

This directory contains comprehensive tests for the `MotelChainService` class, including unit tests and integration tests.

## Test Structure

### 1. Unit Tests (`MotelChainServiceTest.java`)
- **Framework**: JUnit 5 + Mockito
- **Purpose**: Tests business logic in isolation using mocked dependencies
- **Coverage**: All public methods of MotelChainService
- **Test Categories**:
  - Get All Motel Chains Tests
  - Get Motel Chain By ID Tests
  - Create Motel Chain Tests
  - Update Motel Chain Tests
  - Delete Motel Chain Tests

### 2. Integration Tests (`MotelChainServiceIntegrationTest.java`)
- **Framework**: Spring Boot Test + H2 Database
- **Purpose**: Tests the service with real database interactions
- **Database**: H2 in-memory database
- **Coverage**: End-to-end functionality with actual persistence

### 3. Test Suite Runner (`MotelChainServiceTestSuite.java`)
- **Purpose**: Runs all MotelChainService related tests as a single suite
- **Framework**: JUnit Platform Suite

## Dependencies Added

The following testing dependencies have been added to `build.gradle`:

```gradle
// Core testing
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

// Enhanced testing capabilities
testImplementation 'org.mockito:mockito-core:5.5.0'
testImplementation 'org.mockito:mockito-junit-jupiter:5.5.0'
testImplementation 'org.assertj:assertj-core:3.24.2'
testImplementation 'com.h2database:h2:2.2.224'
testImplementation 'org.junit.platform:junit-platform-suite:1.10.0'

// Lombok for tests
testCompileOnly 'org.projectlombok:lombok:1.18.30'
testAnnotationProcessor 'org.projectlombok:lombok:1.18.30'
```

## Test Configuration

### Test Properties (`application-test.properties`)
- Configures H2 in-memory database for integration tests
- Sets up JPA/Hibernate for test environment
- Enables detailed SQL logging for debugging

## Running Tests

### Run All Tests
```bash
# From project root
./gradlew test

# On Windows
gradlew.bat test
```

### Run Specific Test Classes
```bash
# Run only unit tests
./gradlew test --tests "com.example.motels.service.MotelChainServiceTest"

# Run all service tests
./gradlew test --tests "com.example.motels.service.*"

# Run the complete test suite
./gradlew test --tests "com.example.motels.service.MotelChainServiceTestSuite"
```

### Run Tests with Detailed Output
```bash
./gradlew test --info
```

### Run Tests with Coverage Report
```bash
./gradlew test jacocoTestReport
```

## Test Scenarios Covered

### Unit Tests
1. **getAllMotelChains()**
   - Returns all motel chains when data exists
   - Returns empty list when no data exists
   - Handles paginated requests correctly

2. **getMotelChainById(UUID id)**
   - Returns motel chain when ID exists
   - Returns empty Optional when ID doesn't exist

3. **createMotelChain(MotelChain motelChain)**
   - Creates new motel chain when unique combination
   - Returns existing chain when duplicate detected
   - Validates business logic for duplicate prevention

4. **updateMotelChain(UUID id, MotelChain details)**
   - Updates existing motel chain successfully
   - Throws exception when chain not found
   - Updates all fields including nested objects

5. **deleteMotelChain(UUID id)**
   - Deletes existing motel chain successfully
   - Returns false when chain doesn't exist

### Integration Tests
1. **End-to-End Operations**
   - Create and retrieve chain with database persistence
   - Prevent duplicate creation with real database constraints
   - Update chain with actual database transactions
   - Delete chain with database cleanup
   - Pagination with real data set

## Test Data Management

### Mock Data
- Unit tests use Mockito to create controlled test scenarios
- Test data is reset in `@BeforeEach` methods
- Helper methods create consistent test objects

### Database Data
- Integration tests use `@DataJpaTest` for database isolation
- H2 database is recreated for each test class
- Test data is cleaned up automatically between tests

## Best Practices Implemented

1. **Test Isolation**: Each test is independent and doesn't affect others
2. **Descriptive Names**: Test methods have clear, descriptive names
3. **Given-When-Then**: Tests follow AAA (Arrange-Act-Assert) pattern
4. **Comprehensive Coverage**: All public methods and edge cases covered
5. **Proper Mocking**: Dependencies are properly mocked in unit tests
6. **Real Integration**: Integration tests use actual database operations
7. **Readable Assertions**: Using AssertJ for fluent, readable assertions

## Debugging Tests

### Enable Debug Logging
Add to `application-test.properties`:
```properties
logging.level.com.example.motels=DEBUG
logging.level.org.springframework.test=DEBUG
```

### View Generated SQL
Integration tests show actual SQL queries in console output due to:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### H2 Console (for debugging)
Access H2 console during test debugging:
```properties
spring.h2.console.enabled=true
```

## Continuous Integration

These tests are designed to run in CI/CD pipelines:
- Fast execution with in-memory database
- No external dependencies required
- Deterministic results
- Comprehensive error reporting

## Coverage Goals

The test suite aims for:
- **Line Coverage**: >95%
- **Branch Coverage**: >90%
- **Method Coverage**: 100%

Run coverage report to verify:
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```
