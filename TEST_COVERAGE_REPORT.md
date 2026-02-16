# Test Coverage Report

This document describes the test coverage strategy and how to generate coverage reports for the Order & Returns Management System.

## Test Coverage Strategy

### Unit Tests

Unit tests focus on testing individual components in isolation:

1. **State Machine Tests** (`OrderStateMachineTest`, `ReturnStateMachineTest`)
   - Tests all valid state transitions
   - Tests invalid state transitions throw exceptions
   - Tests business rules (e.g., cancellation rules)

2. **Service Layer Tests** (`StateHistoryServiceTest`)
   - Tests business logic
   - Tests validation rules
   - Uses mocks for dependencies

3. **Controller Tests** (`AuthControllerTest`)
   - Tests HTTP request/response handling
   - Tests validation
   - Uses MockMvc for web layer testing

### Integration Tests

Integration tests verify components work together:

1. **Repository Tests**
   - Tests database operations
   - Tests custom query methods
   - Uses in-memory H2 database

2. **End-to-End Tests**
   - Tests complete workflows
   - Tests API endpoints with real database

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

The coverage report will be generated in: `target/site/jacoco/index.html`

### Run Specific Test Class
```bash
mvn test -Dtest=OrderStateMachineTest
```

### Run Tests and Generate Coverage Report
```bash
mvn clean test jacoco:report
```

## Coverage Goals

### Current Coverage Targets

- **Overall Coverage**: Minimum 50%
- **Service Layer**: Minimum 70%
- **State Machines**: Minimum 90%
- **Controllers**: Minimum 60%
- **Repositories**: Minimum 50%

### Coverage by Package

| Package | Target Coverage | Current Status |
|---------|----------------|----------------|
| `service` | 70% | ✅ In Progress |
| `controller` | 60% | ✅ In Progress |
| `repository` | 50% | ⏳ Pending |
| `model` | 30% | ⏳ Pending |
| `security` | 60% | ⏳ Pending |

## Test Files Created

### Unit Tests

1. **OrderStateMachineTest.java**
   - Tests order state transitions
   - Tests cancellation rules
   - 12 test cases

2. **ReturnStateMachineTest.java**
   - Tests return state transitions
   - Tests invalid transitions
   - 9 test cases

3. **StateHistoryServiceTest.java**
   - Tests audit trail logging
   - Tests entity type handling
   - 3 test cases

4. **AuthControllerTest.java**
   - Tests authentication endpoints
   - Tests request validation
   - 3 test cases

### Integration Tests

1. **OrderReturnsManagementApplicationTests.java**
   - Tests Spring context loading
   - 1 test case

## Viewing Coverage Reports

### HTML Report

After running `mvn jacoco:report`, open:
```
target/site/jacoco/index.html
```

### Coverage Metrics

The report shows:
- **Line Coverage**: Percentage of lines executed
- **Branch Coverage**: Percentage of branches executed
- **Method Coverage**: Percentage of methods executed
- **Class Coverage**: Percentage of classes executed

## Continuous Integration

### GitHub Actions Example

```yaml
name: Test and Coverage

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests with coverage
        run: mvn clean test jacoco:report
      - name: Upload coverage report
        uses: codecov/codecov-action@v2
        with:
          file: target/site/jacoco/jacoco.xml
```

## Test Best Practices

### 1. Test Naming
- Use descriptive test names: `testValidTransitionPendingPaymentToPaid()`
- Use `@DisplayName` for readable test descriptions

### 2. Test Structure
- Arrange-Act-Assert (AAA) pattern
- One assertion per test (when possible)
- Test one behavior per test method

### 3. Mocking
- Mock external dependencies
- Use `@Mock` and `@InjectMocks` annotations
- Verify interactions when necessary

### 4. Test Data
- Use test fixtures
- Keep test data minimal and focused
- Use builders for complex objects

## Future Test Enhancements

1. **Service Layer Tests**
   - `OrderServiceTest` - Test order creation, updates, cancellation
   - `ReturnServiceTest` - Test return creation, validation, updates
   - `AuthServiceTest` - Test authentication logic

2. **Repository Tests**
   - `OrderRepositoryTest` - Test custom query methods
   - `ReturnRepositoryTest` - Test return queries
   - `StateHistoryRepositoryTest` - Test audit queries

3. **Integration Tests**
   - End-to-end order workflow test
   - End-to-end return workflow test
   - Authentication flow test

4. **Performance Tests**
   - Load testing for API endpoints
   - Batch job performance tests

## Notes

- Tests use in-memory H2 database for fast execution
- Test profile (`application-test.yml`) disables Flyway and reduces logging
- Mockito is used for mocking dependencies
- Spring Boot Test provides MockMvc for web layer testing

## Coverage Report Location

After running tests with coverage:
- **HTML Report**: `target/site/jacoco/index.html`
- **XML Report**: `target/site/jacoco/jacoco.xml`
- **CSV Report**: `target/site/jacoco/jacoco.csv`

Open the HTML report in a browser to view detailed coverage metrics.


