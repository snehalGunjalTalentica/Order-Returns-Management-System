# Workflow Design Document

This document explains the implementation of state machines, workflows, and database schema for the Order & Returns Management System.

## Table of Contents

1. [Order State Machine](#order-state-machine)
2. [Return State Machine](#return-state-machine)
3. [Database Schema](#database-schema)
4. [State History Storage](#state-history-storage)
5. [Workflow Integration](#workflow-integration)

---

## Order State Machine

### State Machine Diagram

```
                    ┌─────────────────┐
                    │ PENDING_PAYMENT │
                    └────────┬────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
        ┌───────▼───────┐        ┌────────▼────────┐
        │     PAID      │        │    CANCELLED    │
        └───────┬───────┘        └─────────────────┘
                │                        │
        ┌───────┴────────────┐          │
        │                    │          │
┌───────▼──────────┐  ┌──────▼──────┐   │
│ PROCESSING_IN_   │  │ CANCELLED   │   │
│   WAREHOUSE      │  └─────────────┘   │
└───────┬──────────┘                    │
        │                                │
┌───────▼───────┐                        │
│   SHIPPED    │                        │
└───────┬───────┘                        │
        │                                │
┌───────▼────────┐                       │
│   DELIVERED    │                       │
└────────────────┘                       │
                                         │
                    ┌────────────────────┘
                    │
                    │ (Final States)
                    ▼
```

### State Transitions

| Current State | Valid Next States | Business Rule |
|--------------|-------------------|---------------|
| `PENDING_PAYMENT` | `PAID`, `CANCELLED` | Payment confirmed or order cancelled |
| `PAID` | `PROCESSING_IN_WAREHOUSE`, `CANCELLED` | Warehouse processing starts or cancellation |
| `PROCESSING_IN_WAREHOUSE` | `SHIPPED` | Package prepared and shipped |
| `SHIPPED` | `DELIVERED` | Customer receives package → **Triggers Invoice Job** |
| `DELIVERED` | *(None - Final State)* | Order complete, can initiate return |
| `CANCELLED` | *(None - Final State)* | Order cancelled, no further actions |

### Implementation Details

**File**: `src/main/java/com/articurated/service/OrderStateMachine.java`

```java
private Set<OrderStatus> getValidTransitions(OrderStatus currentStatus) {
    return switch (currentStatus) {
        case PENDING_PAYMENT -> EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED);
        case PAID -> EnumSet.of(OrderStatus.PROCESSING_IN_WAREHOUSE, OrderStatus.CANCELLED);
        case PROCESSING_IN_WAREHOUSE -> EnumSet.of(OrderStatus.SHIPPED);
        case SHIPPED -> EnumSet.of(OrderStatus.DELIVERED);
        case DELIVERED -> EnumSet.noneOf(OrderStatus.class);
        case CANCELLED -> EnumSet.noneOf(OrderStatus.class);
    };
}
```

**Validation**: The `validateTransition()` method ensures only valid transitions are allowed, throwing `InvalidStateTransitionException` for invalid attempts.

---

## Return State Machine

### State Machine Diagram

```
                    ┌──────────────┐
                    │   REQUESTED  │
                    └──────┬───────┘
                           │
                ┌──────────┴──────────┐
                │                     │
        ┌───────▼──────┐     ┌────────▼────────┐
        │   APPROVED   │     │    REJECTED      │
        └───────┬──────┘     └──────────────────┘
                │                    │
        ┌───────▼──────────┐        │
        │   IN_TRANSIT     │        │
        └───────┬──────────┘        │
                │                   │
        ┌───────▼──────────┐        │
        │    RECEIVED      │        │
        └───────┬──────────┘        │
                │                   │
        ┌───────▼──────────┐        │
        │   COMPLETED      │        │
        └──────────────────┘        │
                │                   │
                └───────────────────┘
                    (Final States)
```

### State Transitions

| Current State | Valid Next States | Business Rule |
|--------------|-------------------|---------------|
| `REQUESTED` | `APPROVED`, `REJECTED` | Manager reviews and approves/rejects |
| `APPROVED` | `IN_TRANSIT` | Customer ships item back |
| `REJECTED` | *(None - Final State)* | Return request denied |
| `IN_TRANSIT` | `RECEIVED` | Warehouse receives returned item |
| `RECEIVED` | `COMPLETED` | Warehouse confirms receipt → **Triggers Refund Job** |
| `COMPLETED` | *(None - Final State)* | Refund processed, return complete |

### Implementation Details

**File**: `src/main/java/com/articurated/service/ReturnStateMachine.java`

```java
private Set<ReturnStatus> getValidTransitions(ReturnStatus currentStatus) {
    return switch (currentStatus) {
        case REQUESTED -> EnumSet.of(ReturnStatus.APPROVED, ReturnStatus.REJECTED);
        case APPROVED -> EnumSet.of(ReturnStatus.IN_TRANSIT);
        case REJECTED -> EnumSet.noneOf(ReturnStatus.class);
        case IN_TRANSIT -> EnumSet.of(ReturnStatus.RECEIVED);
        case RECEIVED -> EnumSet.of(ReturnStatus.COMPLETED);
        case COMPLETED -> EnumSet.noneOf(ReturnStatus.class);
    };
}
```

**Business Rules**:
- Return can only be created for orders in `DELIVERED` status
- Return must be requested within 7 days of delivery
- Only one active return per order
- Full refund to original payment method

---

## Database Schema

### Entity Relationship Diagram

```
┌─────────────┐
│  Customer   │
│─────────────│
│ id (PK)     │
│ email       │◄────┐
│ password    │     │
│ first_name  │     │
│ last_name   │     │
│ phone       │     │
│ role        │     │
└─────────────┘     │
                    │
                    │ (1:N)
                    │
┌─────────────┐     │
│   Order     │     │
│─────────────│     │
│ id (PK)     │     │
│ customer_id │─────┘
│ order_number│
│ status      │◄────┐
│ total_amount│     │
│ shipping_   │     │
│   address   │     │
│ payment_    │     │
│   method    │     │
└──────┬──────┘     │
       │            │
       │ (1:N)      │
       │            │
┌──────▼──────┐     │
│ OrderItem   │     │
│─────────────│     │
│ id (PK)     │     │
│ order_id    │─────┘
│ product_id  │
│ product_name│
│ quantity    │
│ unit_price  │
│ total_price │
└─────────────┘

┌─────────────┐
│   Return    │
│─────────────│
│ id (PK)     │
│ order_id    │──────┐
│ return_     │      │
│   number    │      │
│ status      │      │
│ return_     │      │
│   reason    │      │
│ refund_     │      │
│   amount    │      │
│ manager_    │      │
│   notes     │      │
└──────┬──────┘      │
       │             │
       │ (1:N)       │
       │             │
┌──────▼─────────────▼──────┐
│      StateHistory         │
│───────────────────────────│
│ id (PK)                   │
│ entity_type (ORDER/RETURN) │
│ entity_id                  │
│ previous_status            │
│ new_status                 │
│ changed_by                 │
│ change_reason              │
│ created_at                 │
└────────────────────────────┘
```

### Table Descriptions

#### 1. `customers` Table

**Purpose**: Stores customer information and authentication details.

**Key Fields**:
- `id`: Primary key
- `email`: Unique identifier for login
- `password`: BCrypt encrypted password
- `role`: Enum (CUSTOMER, ADMIN, MANAGER)

**Relationships**:
- One-to-Many with `orders`

---

#### 2. `orders` Table

**Purpose**: Stores order information and current state.

**Key Fields**:
- `id`: Primary key
- `customer_id`: Foreign key to `customers`
- `order_number`: Unique order identifier (e.g., ORD-XXXXXXXX)
- `status`: Enum (PENDING_PAYMENT, PAID, PROCESSING_IN_WAREHOUSE, SHIPPED, DELIVERED, CANCELLED)
- `total_amount`: Decimal value for order total
- `shipping_address`: Text field for delivery address
- `payment_method`: Payment method used
- `payment_transaction_id`: Transaction reference

**Relationships**:
- Many-to-One with `customers`
- One-to-Many with `order_items`
- One-to-Many with `returns`
- One-to-Many with `state_history`

---

#### 3. `order_items` Table

**Purpose**: Stores individual line items within an order.

**Key Fields**:
- `id`: Primary key
- `order_id`: Foreign key to `orders`
- `product_id`: Product identifier
- `product_name`: Product name
- `quantity`: Number of items
- `unit_price`: Price per unit
- `total_price`: Calculated total (quantity × unit_price)

**Relationships**:
- Many-to-One with `orders`

---

#### 4. `returns` Table

**Purpose**: Stores return request information and current state.

**Key Fields**:
- `id`: Primary key
- `order_id`: Foreign key to `orders`
- `return_number`: Unique return identifier (e.g., RET-XXXXXXXX)
- `status`: Enum (REQUESTED, APPROVED, REJECTED, IN_TRANSIT, RECEIVED, COMPLETED)
- `return_reason`: Free text reason for return
- `refund_amount`: Amount to be refunded (full order amount)
- `manager_notes`: Optional notes from manager

**Relationships**:
- Many-to-One with `orders`
- One-to-Many with `state_history`

---

#### 5. `state_history` Table

**Purpose**: Audit trail for all state changes in orders and returns.

**Key Fields**:
- `id`: Primary key
- `entity_type`: Enum (ORDER, RETURN)
- `entity_id`: Foreign key to the entity (order_id or return_id)
- `previous_status`: Previous state value
- `new_status`: New state value
- `changed_by`: User who made the change (email)
- `change_reason`: Optional reason for the change
- `created_at`: Timestamp of the change

**Relationships**:
- Polymorphic relationship with both `orders` and `returns`

**Why This Design**: 
- Single table for all state changes (polymorphic)
- Enables complete audit trail
- Easy to query history for any entity
- Supports compliance and debugging

---

## State History Storage

### How State History Works

1. **Logging Trigger**: Every state change in `OrderService` or `ReturnService` calls `StateHistoryService.logStateChange()`

2. **Storage Process**:
   ```
   State Change Event
        ↓
   StateHistoryService.logStateChange()
        ↓
   Creates StateHistory Entity
        ↓
   Sets entity_type (ORDER/RETURN)
        ↓
   Sets entity_id (order.id or return.id)
        ↓
   Records previous_status and new_status
        ↓
   Records changed_by (user email)
        ↓
   Records change_reason (optional)
        ↓
   Saves to state_history table
   ```

3. **Querying History**:
   ```java
   // Get all state changes for an order
   stateHistoryService.getStateHistory(EntityType.ORDER, orderId);
   
   // Returns chronological list of all state transitions
   ```

### Example State History Record

```json
{
  "id": 1,
  "entityType": "ORDER",
  "entityId": 123,
  "previousStatus": "PAID",
  "newStatus": "PROCESSING_IN_WAREHOUSE",
  "changedBy": "admin@articurated.com",
  "changeReason": "Order moved to warehouse",
  "createdAt": "2024-01-15T10:30:00"
}
```

---

## Workflow Integration

### Order Lifecycle Workflow

```
1. Customer creates order
   → Order created with status: PENDING_PAYMENT
   → StateHistory logged: (null → PENDING_PAYMENT)

2. Payment confirmed
   → Admin updates status: PAID
   → StateHistory logged: (PENDING_PAYMENT → PAID)

3. Warehouse processing
   → Admin updates status: PROCESSING_IN_WAREHOUSE
   → StateHistory logged: (PAID → PROCESSING_IN_WAREHOUSE)

4. Order shipped
   → Admin updates status: SHIPPED
   → StateHistory logged: (PROCESSING_IN_WAREHOUSE → SHIPPED)
   → Background Job Triggered: Invoice Generation

5. Order delivered
   → Admin updates status: DELIVERED
   → StateHistory logged: (SHIPPED → DELIVERED)
   → Order complete, return can be initiated
```

### Return Lifecycle Workflow

```
1. Customer requests return
   → Return created with status: REQUESTED
   → Validates: Order is DELIVERED, within 7 days
   → StateHistory logged: (null → REQUESTED)

2. Manager reviews
   → Manager updates status: APPROVED or REJECTED
   → StateHistory logged: (REQUESTED → APPROVED/REJECTED)

3. Customer ships back (if approved)
   → Manager updates status: IN_TRANSIT
   → StateHistory logged: (APPROVED → IN_TRANSIT)

4. Warehouse receives
   → Manager updates status: RECEIVED
   → StateHistory logged: (IN_TRANSIT → RECEIVED)

5. Refund processed
   → Manager updates status: COMPLETED
   → StateHistory logged: (RECEIVED → COMPLETED)
   → Background Job Triggered: Refund Processing
```

---

## Design Decisions

### Why State Machines?

1. **Enforces Business Rules**: Prevents invalid state transitions
2. **Type Safety**: Enum-based states prevent typos and invalid values
3. **Maintainability**: Clear definition of valid transitions
4. **Testability**: Easy to test state transition logic

### Why Separate State History Table?

1. **Audit Compliance**: Complete history of all changes
2. **Debugging**: Track when and why states changed
3. **Analytics**: Analyze state transition patterns
4. **Non-Intrusive**: Doesn't modify main entity tables

### Why Polymorphic State History?

1. **DRY Principle**: Single table for both orders and returns
2. **Consistency**: Same audit pattern for all entities
3. **Flexibility**: Easy to add state tracking to new entities
4. **Query Efficiency**: Single table to query for all history

---

## Future Enhancements

1. **State Machine Visualization**: Add visual state diagrams in UI
2. **State Transition Rules**: Configurable transition rules
3. **State History Analytics**: Dashboard for state transition metrics
4. **Event Sourcing**: Consider event sourcing for complete audit trail
5. **State Machine Framework**: Consider using Spring State Machine framework


