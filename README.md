| Service | Holds business rules and transaction boundaries. |
| Repository | Performs data access through Spring Data JPA. |
| Entity | Maps Java objects and relationships to database tables. |
| DTO | Defines the API contract without exposing JPA entity graphs. |
| Exception advice | Converts exceptions into consistent, safe API error responses. |

## Project structure

```text
src/main/java/com/ecommerce/project
├── controllers/   # HTTP endpoints
├── service/       # Business rules and transactions
├── repositories/  # JPA data access
├── models/        # JPA entities
├── dto/           # Request and response contracts
└── exceptions/    # Global API error handling
```

## Implemented features

### Catalogue

- Create, read, update, and delete products
- Create, read, update, and delete categories
- Filter products by product ID or category ID
- Optimistic locking via `@Version` for concurrent entity updates

### Orders and inventory

- `POST /api/orders` validates an order request before processing it.
- Inventory is updated in one conditional database statement:

  ```sql
  UPDATE product
  SET quantity = quantity - :requestedQuantity
  WHERE id = :productId AND quantity >= :requestedQuantity
  ```

  This means concurrent requests cannot both purchase the same final unit.
- The order flow is wrapped in `@Transactional`. If one line item has insufficient stock, no order is created and previous stock deductions are rolled back.
- `GET /api/orders` returns response DTOs rather than directly serializing the bidirectional JPA entity graph.

## API endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/public/products` | List products; accepts optional `id` and `categoryId` query parameters. |
| `POST` | `/api/public/products` | Create a product. |
| `PATCH` | `/api/public/products?id={id}` | Update product fields. |
| `DELETE` | `/api/private/products?id={id}` | Delete a product. |
| `GET` | `/api/public/categories` | List categories. |
| `POST` | `/api/public/categories` | Create a category. |
| `PATCH` | `/api/public/categories/{id}` | Update a category. |
| `DELETE` | `/api/public/categories/{id}` | Delete a category. |
| `GET` | `/api/orders` | List orders. |
| `POST` | `/api/orders` | Place an order and reserve stock atomically. |

### Place an order

```http
POST /api/orders
Content-Type: application/json
```

```json
{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 5, "quantity": 1 }
  ]
}
```

Successful requests return `201 Created`:

```json
{
  "orderId": 12,
  "message": "Order placed successfully"
}
```

When stock is unavailable, the API returns `409 Conflict`. Invalid payloads return `400 Bad Request`.

## Run locally

### Prerequisites

- JDK 25
- MySQL 8+
- Maven (or use the included Maven wrapper)

Create a database named `microservice`, then configure the datasource in `src/main/resources/application.properties`. Do not commit real database credentials; use environment-specific configuration or environment variables for shared/production deployments.

Run the application:

```powershell
.\mvnw.cmd spring-boot:run
```

Run tests:

```powershell
.\mvnw.cmd test
```

## Concurrency notes

There are two complementary concurrency strategies in this project:

- **Optimistic locking** (`@Version`) detects stale updates to a product/category and should return `409 Conflict` rather than silently overwriting a newer change.
- **Atomic conditional inventory update** is used for checkout. It is a better fit for frequent quantity decrements because it checks and decrements stock in a single database operation.

Java `synchronized` is intentionally not used for inventory: it only coordinates requests within one JVM and fails when the API is scaled to multiple application instances. Database-backed concurrency control works in both single-instance and horizontally scaled deployments.

## Roadmap

- [ ] Authentication and authorization with role-based access control
- [ ] Customer profiles and delivery addresses
- [ ] Shopping cart APIs
- [ ] Product price, order totals, and immutable price snapshots on order items
- [ ] Order lifecycle: `PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`
- [ ] Payment integration and idempotency keys to prevent duplicate checkout submissions
- [ ] Stock reservations with expiry for long-running payment workflows
- [ ] Pagination, sorting, search, and filtering
- [ ] Flyway or Liquibase database migrations
- [ ] OpenAPI/Swagger documentation
- [ ] Unit, integration, and concurrent-order test coverage
- [ ] Docker-based local development and CI/CD pipeline

## Design principles

1. Keep controllers thin.
2. Put business decisions and transactions in services.
3. Keep persistence logic in repositories.
4. Use DTOs at the API boundary; do not expose entities as your public contract.
5. Let the database enforce inventory consistency.
6. Return meaningful HTTP status codes and consistent error responses.

---

Built with Java, Spring Boot, Spring MVC, Spring Data JPA, Bean Validation, and MySQL.
