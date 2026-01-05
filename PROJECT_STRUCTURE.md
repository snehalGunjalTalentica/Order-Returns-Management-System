# Project Structure

This document explains the structure of the Order & Returns Management System project and the purpose of each folder and key module.

## Root Directory Structure

```
Order & Returns Management System/
├── src/
│   ├── main/
│   │   ├── java/com/articurated/
│   │   └── resources/
│   └── test/
├── pom.xml
├── docker-compose.yml
├── README.md
├── PROJECT_STRUCTURE.md
├── WORKFLOW_DESIGN.md
├── API-SPECIFICATION.yml
├── CHAT_HISTORY.md
└── .gitignore
```

## Source Code Structure (`src/main/java/com/articurated/`)

### `/batch` - Spring Batch Job Configurations

**Purpose**: Contains Spring Batch job definitions for asynchronous background processing.

**Key Files**:
- `batch/config/BatchConfig.java` - Batch configuration and job launcher setup
- `batch/job/InvoiceGenerationJob.java` - Job for generating PDF invoices when orders are shipped
- `batch/job/RefundProcessingJob.java` - Job for processing refunds when returns are completed

**Why**: Separates background job logic from main application flow, enabling asynchronous processing of time-consuming tasks.

---

### `/controller` - REST API Controllers

**Purpose**: Handles HTTP requests, validates input, and returns responses. Implements the API layer.

**Key Files**:
- `controller/AuthController.java` - Authentication endpoints (register, login)
- `controller/OrderController.java` - Order management endpoints (CRUD, status updates, cancellation)
- `controller/ReturnController.java` - Return management endpoints (create, update status, history)
- `controller/MockPaymentGatewayController.java` - Mock payment gateway for testing refunds

**Why**: Follows RESTful principles, separates HTTP concerns from business logic, and provides API versioning (v1).

---

### `/dto` - Data Transfer Objects

**Purpose**: Defines request/response structures for API communication. Separates API contracts from internal entities.

**Key Files**:
- `dto/AuthRequest.java`, `dto/AuthResponse.java` - Authentication DTOs
- `dto/CreateOrderRequest.java`, `dto/OrderResponse.java` - Order DTOs
- `dto/CreateReturnRequest.java`, `dto/ReturnResponse.java` - Return DTOs
- `dto/StateHistoryResponse.java` - Audit trail DTOs

**Why**: Prevents exposing internal entity structure, provides validation annotations, and enables API versioning flexibility.

---

### `/exception` - Exception Handling

**Purpose**: Centralized exception handling and custom exception definitions.

**Key Files**:
- `exception/GlobalExceptionHandler.java` - Global exception handler with standardized error responses
- `exception/ResourceNotFoundException.java` - Custom exception for missing resources
- `exception/InvalidStateTransitionException.java` - Custom exception for invalid state transitions

**Why**: Provides consistent error responses across the API and separates error handling logic.

---

### `/model` - Entity Models

**Purpose**: JPA entity classes representing database tables. Defines the domain model.

**Key Files**:
- `model/Customer.java` - Customer entity with authentication info
- `model/Order.java` - Order entity with state management
- `model/OrderItem.java` - Order line items
- `model/Return.java` - Return request entity
- `model/StateHistory.java` - Audit trail entity
- `model/enums/` - Enumeration types (OrderStatus, ReturnStatus, Role, EntityType)

**Why**: Maps database schema to Java objects, defines relationships, and encapsulates domain logic.

---

### `/repository` - Data Access Layer

**Purpose**: JPA repositories providing database access methods. Implements the repository pattern.

**Key Files**:
- `repository/CustomerRepository.java` - Customer data access
- `repository/OrderRepository.java` - Order data access with custom queries
- `repository/OrderItemRepository.java` - Order item data access
- `repository/ReturnRepository.java` - Return data access
- `repository/StateHistoryRepository.java` - Audit trail data access

**Why**: Abstracts database operations, provides type-safe queries, and enables easy testing with in-memory databases.

---

### `/security` - Security Configuration

**Purpose**: JWT authentication and authorization setup. Implements Spring Security configuration.

**Key Files**:
- `security/SecurityConfig.java` - Main security configuration with role-based access control
- `security/JwtTokenProvider.java` - JWT token generation and validation
- `security/JwtAuthenticationFilter.java` - Filter to extract and validate JWT tokens
- `security/CustomUserDetailsService.java` - Loads user details for authentication

**Why**: Centralizes security logic, implements stateless authentication, and enforces role-based access control.

---

### `/service` - Business Logic Layer

**Purpose**: Contains business logic, state machine implementations, and orchestrates operations.

**Key Files**:
- `service/OrderService.java` - Order business logic (creation, status updates, cancellation)
- `service/ReturnService.java` - Return business logic (creation, validation, status updates)
- `service/AuthService.java` - Authentication business logic (registration, login)
- `service/OrderStateMachine.java` - Order state transition validation
- `service/ReturnStateMachine.java` - Return state transition validation
- `service/StateHistoryService.java` - Audit trail logging
- `service/PdfInvoiceService.java` - PDF invoice generation
- `service/PaymentGatewayService.java` - Payment gateway integration
- `service/JobTriggerService.java` - Background job triggering

**Why**: Separates business rules from controllers and repositories, implements state machines, and encapsulates complex logic.

---

## Resources Structure (`src/main/resources/`)

### `/db/migration` - Flyway Migration Scripts

**Purpose**: Database schema versioning and migration scripts.

**Key Files**:
- `V1__Create_customers_table.sql` - Creates customers table
- `V2__Create_orders_table.sql` - Creates orders table
- `V3__Create_order_items_table.sql` - Creates order_items table
- `V4__Create_returns_table.sql` - Creates returns table
- `V5__Create_state_history_table.sql` - Creates state_history table

**Why**: Version-controlled database schema, enables reproducible deployments, and tracks schema changes.

---

### `application.yml` - Application Configuration

**Purpose**: Centralized configuration for the application.

**Key Sections**:
- Database configuration (H2)
- Flyway settings
- JWT settings
- Spring Batch configuration
- Payment gateway settings
- Logging configuration

**Why**: Externalizes configuration, enables environment-specific settings, and follows Spring Boot conventions.

---

## Test Structure (`src/test/`)

**Purpose**: Unit and integration tests for the application.

**Key Areas**:
- Service layer tests
- Controller tests
- Repository tests
- State machine tests
- Integration tests

**Why**: Ensures code quality, validates business logic, and provides regression testing.

---

## Key Design Patterns Used

1. **Repository Pattern**: Data access abstraction
2. **Service Layer Pattern**: Business logic separation
3. **DTO Pattern**: API contract separation
4. **State Machine Pattern**: Order/Return state management
5. **Strategy Pattern**: State transition validation
6. **Filter Pattern**: JWT authentication
7. **Exception Handler Pattern**: Centralized error handling

---

## Module Dependencies

```
Controllers → Services → Repositories → Database
     ↓           ↓
    DTOs    State Machines
     ↓           ↓
  Validation  Exceptions
```

---

## Why This Structure?

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Testability**: Easy to mock dependencies and test in isolation
3. **Maintainability**: Clear organization makes code easy to navigate
4. **Scalability**: Structure supports adding new features without major refactoring
5. **Standards**: Follows Spring Boot best practices and conventions

