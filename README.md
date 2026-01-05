# Order & Returns Management System

A robust backend application built with Spring Boot 2.x for managing the complete lifecycle of orders and returns for ArtiCurated, a boutique online marketplace.

## Features

- **Complex Order State Management**: State machine implementation for order lifecycle (PENDING_PAYMENT → PAID → PROCESSING_IN_WAREHOUSE → SHIPPED → DELIVERED, with CANCELLED option)
- **Multi-Step Returns Workflow**: State machine for returns (REQUESTED → APPROVED/REJECTED → IN_TRANSIT → RECEIVED → COMPLETED)
- **Asynchronous Background Jobs**: Spring Batch jobs for PDF invoice generation and refund processing
- **JWT Authentication**: Secure authentication with role-based access control (CUSTOMER, ADMIN, MANAGER)
- **State History Tracking**: Complete audit trail for all state changes
- **RESTful API**: Versioned API (v1) with comprehensive endpoints

## Technology Stack

- **Java 17**
- **Spring Boot 2.7.18**
- **Spring Batch** - For background job processing
- **Spring Security** - JWT-based authentication
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Flyway** - Database migration management
- **Maven** - Build tool
- **iTextPDF** - PDF generation
- **WebFlux** - For payment gateway integration

## Project Structure

```
src/
├── main/
│   ├── java/com/articurated/
│   │   ├── batch/          # Spring Batch job configurations
│   │   ├── controller/     # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Exception handling
│   │   ├── model/          # Entity models
│   │   ├── repository/     # JPA repositories
│   │   ├── security/       # JWT and security configuration
│   │   └── service/        # Business logic services
│   └── resources/
│       ├── db/migration/   # Flyway migration scripts
│       └── application.yml # Application configuration
└── test/
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (optional, for containerized setup)

### Database Setup

The application uses **H2 in-memory database** with **Flyway** for schema management.

#### Automatic Setup (Default)

The database schema is automatically created when the application starts:
1. Flyway migrations run automatically on startup
2. All tables are created in the H2 in-memory database
3. No manual database setup required

#### Manual Database Access

Access the H2 database console at: `http://localhost:8080/h2-console`

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:articurateddb`
- Username: `sa`
- Password: (leave empty)

**Note:** The database is in-memory, so data is lost when the application stops.

### Running the Application

#### Option 1: Using Maven

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd "Order & Returns Management System"
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

#### Option 2: Using Docker Compose

1. Navigate to the project directory
2. Run Docker Compose:
   ```bash
   docker-compose up
   ```

This will start the application and all required services. See `docker-compose.yml` for details.

### Background Workers Setup

The application uses **Spring Batch** for background job processing. Background jobs are configured to run automatically:

#### Invoice Generation Job
- **Trigger**: Automatically processes orders with status `SHIPPED`
- **Function**: Generates PDF invoices and simulates email sending
- **Configuration**: See `src/main/java/com/articurated/batch/job/InvoiceGenerationJob.java`

#### Refund Processing Job
- **Trigger**: Automatically processes returns with status `COMPLETED`
- **Function**: Calls payment gateway API to process refunds
- **Configuration**: See `src/main/java/com/articurated/batch/job/RefundProcessingJob.java`

#### Running Background Jobs Manually

Background jobs are configured with `spring.batch.job.enabled=false` by default. To enable automatic job execution:

1. Update `application.yml`:
   ```yaml
   spring:
     batch:
       job:
         enabled: true
   ```

2. Or trigger jobs programmatically using `JobTriggerService`

#### Monitoring Background Jobs

- Spring Batch creates metadata tables automatically (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, etc.)
- Query these tables to monitor job execution status
- Access via H2 console: `http://localhost:8080/h2-console`

## API Endpoints

### Authentication

- `POST /api/v1/auth/register` - Register a new customer
- `POST /api/v1/auth/register/admin` - Register an admin user
- `POST /api/v1/auth/register/manager` - Register a manager user
- `POST /api/v1/auth/login` - Login and get JWT token

### Orders

- `POST /api/v1/orders` - Create a new order (CUSTOMER, ADMIN)
- `GET /api/v1/orders/{id}` - Get order by ID
- `GET /api/v1/orders/order-number/{orderNumber}` - Get order by order number
- `GET /api/v1/orders/customer/{customerId}` - Get all orders for a customer
- `PUT /api/v1/orders/{id}/status` - Update order status (ADMIN only)
- `POST /api/v1/orders/{id}/cancel` - Cancel an order (CUSTOMER, ADMIN)
- `GET /api/v1/orders/{id}/history` - Get state history for an order

### Returns

- `POST /api/v1/returns/order/{orderId}` - Create a return request (CUSTOMER, ADMIN)
- `GET /api/v1/returns/{id}` - Get return by ID
- `GET /api/v1/returns/return-number/{returnNumber}` - Get return by return number
- `GET /api/v1/returns/order/{orderId}` - Get all returns for an order
- `PUT /api/v1/returns/{id}/status` - Update return status (ADMIN, MANAGER)
- `GET /api/v1/returns/{id}/history` - Get state history for a return

### Mock Payment Gateway

- `POST /mock-payment-gateway/refund` - Mock endpoint for refund processing

## Business Rules

### Order State Transitions

- **PENDING_PAYMENT** → PAID, CANCELLED
- **PAID** → PROCESSING_IN_WAREHOUSE, CANCELLED
- **PROCESSING_IN_WAREHOUSE** → SHIPPED
- **SHIPPED** → DELIVERED (triggers PDF invoice generation)
- **DELIVERED** → No transitions (final state)
- **CANCELLED** → No transitions (final state)

### Return State Transitions

- **REQUESTED** → APPROVED, REJECTED
- **APPROVED** → IN_TRANSIT
- **REJECTED** → No transitions (final state)
- **IN_TRANSIT** → RECEIVED
- **RECEIVED** → COMPLETED (triggers refund processing)
- **COMPLETED** → No transitions (final state)

### Return Rules

- Returns can only be initiated for orders in DELIVERED status
- Returns must be requested within 7 days of delivery
- Only one active return per order
- Full refund to original payment method

## Background Jobs

### Invoice Generation Job

- Triggered when an order status changes to SHIPPED
- Generates PDF invoice using iTextPDF
- Simulates email sending to customer

### Refund Processing Job

- Triggered when a return status changes to COMPLETED
- Makes API call to payment gateway to process refund
- Handles refund failures gracefully

## Security

- JWT token-based authentication
- Role-based access control (RBAC)
- Password encryption using BCrypt
- Token expiration: 24 hours

## Database Schema

The application uses Flyway for database migrations. All migration scripts are located in `src/main/resources/db/migration/`:

- `V1__Create_customers_table.sql`
- `V2__Create_orders_table.sql`
- `V3__Create_order_items_table.sql`
- `V4__Create_returns_table.sql`
- `V5__Create_state_history_table.sql`

## Testing

Run tests with:
```bash
mvn test
```

## Configuration

Key configuration in `application.yml`:

- JWT secret and expiration
- H2 database settings
- Payment gateway URL
- Batch job settings

## Future Enhancements

- Add invoice generation flag to prevent duplicate processing
- Add refund processing flag to prevent duplicate refunds
- Implement proper email service integration
- Add comprehensive unit and integration tests
- Add API documentation with Swagger/OpenAPI
- Implement scheduled batch jobs for processing pending invoices/refunds
- Add pagination for list endpoints
- Enhance error handling and logging

## License

This project is part of the ArtiCurated Order & Returns Management System.



"# Order-Returns-Management-System" 
