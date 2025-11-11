# ING Brokerage Firm API

A comprehensive Spring Boot-based brokerage firm management system that handles customer operations, stock orders, and asset management with
robust security and transactional integrity.

## ✨ Features

### Customer Management

- Create, read, update, and delete customer accounts
- Role-based access control (ADMIN, CUSTOMER)
- Secure password encryption with BCrypt
- Username uniqueness validation

### Order Management

- Create BUY/SELL stock orders
- Order status tracking (PENDING, MATCHED, CANCELLED)
- Date range filtering and pagination
- Admin-only order matching and cancellation

### Asset Management

- Real-time asset tracking per customer
- Automatic asset locking/unlocking during order lifecycle
- Pessimistic locking for concurrent transaction safety
- Support for multiple asset types including TRY (Turkish Lira)

### Security Features

- JWT-based authentication
- Method-level security with `@PreAuthorize`
- Self-or-admin access control pattern
- Protected endpoints with role validation

## 🛠 Tech Stack

- **Framework:** Spring Boot 3.5.7
- **Language:** Java 21
- **Database:** H2
- **ORM:** Spring Data JPA with Hibernate
- **Security:** Spring Security with JWT
- **Testing:** JUnit 5, Mockito, MockMvc
- **Mapping:** MapStruct
- **Validation:** Jakarta Bean Validation
- **Documentation:** SpringDoc OpenAPI
- **Build Tool:** Maven

## 🏗 Architecture

### Key Design Patterns

- **Repository Pattern:** Data access abstraction
- **DTO Pattern:** Request/Response separation
- **Specification Pattern:** Dynamic query building
- **Service Layer Pattern:** Business logic encapsulation

## 🚀 Getting Started

### Prerequisites

```bash
Java 17+
H2 Database
Maven 3.8+ or Gradle 7+
```

### Installation

**Clone the repository**

```bash
git clone https://github.com/your-org/ing-brokerage-api.git
cd ing-brokerage-api
```

**Build the project**

```bash
mvn clean install
```

**Run the application**

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8081`

## 📚 API Documentation

Once the application is running, visit:

- Swagger UI: `http://localhost:8081/brokerage/api/swagger-ui/index.html#/`
- OpenAPI Spec: `http://localhost:8081/brokerage/api/v3/api-docs`

### Core Endpoints

#### Customer Management

```http
POST   /v1/customers              # Create customer (ADMIN only)
GET    /v1/customers              # List customers (ADMIN only)
GET    /v1/customers/{id}         # Get customer (Self or ADMIN)
PATCH  /v1/customers/{id}         # Update customer (Self or ADMIN)
DELETE /v1/customers/{id}         # Delete customer (ADMIN only)
```

#### Order Management

```http
POST   /v1/orders                 # Create order (Self or ADMIN)
GET    /v1/orders                 # List orders (Self or ADMIN)
PATCH  /v1/orders/{id}            # Match order (ADMIN only)
DELETE /v1/orders/{id}            # Cancel order (ADMIN only)
```

#### Asset Management

```http
GET    /v1/assets                 # List assets (Self or ADMIN)
```

## 🔐 Security

### Authentication Flow

- Default Admin username: admin password: Admin123!

### Security Rules

- **ADMIN** role can:
    - Create/delete customers
    - View all customers and orders
    - Match/cancel orders

- **CUSTOMER** role can:
    - View/update own profile
    - Create orders for self
    - View own orders and assets

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CustomerServiceTest

# Run with coverage
mvn verify
```

### Test Coverage

- Unit tests for all service methods
- Integration tests for controllers with MockMvc
- Security tests for authorization rules
- Exception handling tests

## 📋 Business Rules

### Order Creation Rules

1. **TRY Asset Orders**
    - Price must be exactly 1.00
    - Handled differently in matching logic

2. **BUY Orders**
    - Lock TRY assets (price × size)
    - Verify customer has sufficient TRY balance

3. **SELL Orders**
    - Lock specific asset (size amount)
    - Verify customer owns the asset

### Order Matching Rules

1. **BUY Order Matching**
    - Deduct locked TRY amount
    - Add bought asset to customer's portfolio
    - Update both `size` and `usable_size`

2. **SELL Order Matching**
    - Deduct sold asset from portfolio
    - Add TRY proceeds to customer's balance

### Asset Locking Mechanism

```
size          # Total amount owned
usable_size   # Available for trading

When order is PENDING:
  usable_size -= locked_amount

When order is MATCHED/CANCELLED:
  usable_size updated accordingly
```

## 🐛 Exception Handling

### Custom Exceptions

- `RecordNotFoundException` - 404 Not Found
- `BusinessException` - 400 Bad Request
- Validation errors - 400 Bad Request
- Authentication errors - 401 Unauthorized

### Exception Messages

Located in `ExceptionConstants`:

- `CUSTOMER_NOT_FOUND`
- `ORDER_NOT_FOUND`
- `USERNAME_ALREADY_EXISTS`
- `INSUFFICIENT_ASSET`
- `NO_ASSET_TO_UNLOCK`
- `ORDER_STATUS_NOT_PENDING`
- `TRY_ORDER_PRICE_MUST_BE_ONE`

## 📈 Performance Considerations

- **Pagination:** All list endpoints support pagination
- **Indexing:** Database indexes on foreign keys
- **Query Optimization:** JPA Specification for dynamic queries
- **Transaction Management:** Proper transaction boundaries
- **Locking Strategy:** Pessimistic locking for critical sections

## 🤝 Contributing

### Code Style Guidelines

1. Use Lombok annotations (`@Getter`, `@Setter`, `@RequiredArgsConstructor`)
2. Follow clean architecture principles
3. Write comprehensive unit tests
4. Use meaningful test method names: `should...When...`
5. Validate all inputs with Jakarta Validation
6. Handle all exceptions properly
7. Use MapStruct for entity-DTO mapping