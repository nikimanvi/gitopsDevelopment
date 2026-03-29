# REST API Integration Guide

This guide explains how to run and test the microservices with REST API endpoints.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENT / API GATEWAY                     │
└──────────┬──────────────────┬────────────────┬──────────────┘
           │                  │                │
    HTTP GET/POST/PUT/DELETE over network
           │                  │                │
    ┌──────▼──────┐    ┌──────▼──────┐    ┌──▼───────────┐
    │UserService  │    │ProductService│   │OrderService  │
    │:8081        │    │:8082         │   │:8083         │
    └──────┬──────┘    └──────┬──────┘    └──┬───────────┘
           │                  │                │
    ┌──────▼──────┐    ┌──────▼──────┐    ┌──▼───────────┐
    │UserRepo     │    │ProductRepo   │   │OrderRepo     │
    │(In-Memory)  │    │(In-Memory)   │   │(In-Memory)   │
    └─────────────┘    └──────────────┘    └──────────────┘
```

## Building the Project

First, compile and build the Spring Boot application:

```bash
cd d:\Microservices
mvn clean install
```

## Running Individual Services

### Method 1: Using Spring Boot Maven Plugin

**Terminal 1 - User Service (Port 8081):**
```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.example.app.user.UserServiceApplication -Dspring-boot.run.arguments="--server.port=8081"
```

**Terminal 2 - Product Service (Port 8082):**
```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.example.app.product.ProductServiceApplication -Dspring-boot.run.arguments="--server.port=8082"
```

**Terminal 3 - Order Service (Port 8083):**
```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.example.app.order.OrderServiceApplication -Dspring-boot.run.arguments="--server.port=8083"
```

### Method 2: Using JAR File

```bash
# Build JAR
mvn package -DskipTests

# Run each service in separate terminals
java -cp target/microservices-1.0-SNAPSHOT.jar org.springframework.boot.loader.JarLauncher com.example.app.user.UserServiceApplication --server.port=8081

java -cp target/microservices-1.0-SNAPSHOT.jar org.springframework.boot.loader.JarLauncher com.example.app.product.ProductServiceApplication --server.port=8082

java -cp target/microservices-1.0-SNAPSHOT.jar org.springframework.boot.loader.JarLauncher com.example.app.order.OrderServiceApplication --server.port=8083
```

---

## REST API Endpoints

### User Service (Port 8081)

**Base URL:** `http://localhost:8081/api/users`

#### 1. Get All Users
```
GET /api/users
```
**Example:**
```bash
curl http://localhost:8081/api/users
```

**Response:**
```json
{
  "status": "success",
  "message": "Users retrieved successfully",
  "data": [],
  "error": null
}
```

#### 2. Create a User
```
POST /api/users
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "address": "123 Main Street"
}
```

**Example:**
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+1234567890",
    "address": "123 Main Street"
  }'
```

**Response:**
```json
{
  "status": "success",
  "message": "User created successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+1234567890",
    "address": "123 Main Street",
    "createdAt": 1704384932123
  },
  "error": null
}
```

#### 3. Get User by ID
```
GET /api/users/{id}
```

**Example:**
```bash
curl http://localhost:8081/api/users/1
```

#### 4. Update User
```
PUT /api/users/{id}
Content-Type: application/json
```

**Example:**
```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "john.smith@example.com",
    "phone": "+9876543210",
    "address": "456 Oak Avenue"
  }'
```

#### 5. Delete User
```
DELETE /api/users/{id}
```

**Example:**
```bash
curl -X DELETE http://localhost:8081/api/users/1
```

#### 6. Get User Count
```
GET /api/users/count
```

---

### Product Service (Port 8082)

**Base URL:** `http://localhost:8082/api/products`

#### 1. Get All Products
```
GET /api/products
```

**Example:**
```bash
curl http://localhost:8082/api/products
```

#### 2. Create a Product
```
POST /api/products
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Laptop",
  "description": "High-performance laptop with 16GB RAM",
  "price": 999.99,
  "quantity": 10
}
```

**Example:**
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop with 16GB RAM",
    "price": 999.99,
    "quantity": 10
  }'
```

#### 3. Get Product by ID
```
GET /api/products/{id}
```

**Example:**
```bash
curl http://localhost:8082/api/products/1
```

#### 4. Update Product
```
PUT /api/products/{id}
Content-Type: application/json
```

**Example:**
```bash
curl -X PUT http://localhost:8082/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Pro",
    "description": "Professional laptop with 32GB RAM",
    "price": 1499.99,
    "quantity": 5
  }'
```

#### 5. Delete Product
```
DELETE /api/products/{id}
```

**Example:**
```bash
curl -X DELETE http://localhost:8082/api/products/1
```

#### 6. Check Stock Availability
```
GET /api/products/{id}/stock
```

**Example:**
```bash
curl http://localhost:8082/api/products/1/stock
```

#### 7. Purchase Product (Reduce Stock)
```
POST /api/products/{id}/purchase
Content-Type: application/json
```

**Request Body:**
```json
{
  "quantity": 2
}
```

**Example:**
```bash
curl -X POST http://localhost:8082/api/products/1/purchase \
  -H "Content-Type: application/json" \
  -d '{"quantity": 2}'
```

---

### Order Service (Port 8083)

**Base URL:** `http://localhost:8083/api/orders`

This service demonstrates **cross-service communication**:
- Validates users by calling User Service
- Validates products by calling Product Service
- Updates inventory via Product Service

#### 1. Get All Orders
```
GET /api/orders
```

**Example:**
```bash
curl http://localhost:8083/api/orders
```

#### 2. Place an Order (Cross-Service Integration)
```
POST /api/orders
Content-Type: application/json
```

**Request Body:**
```json
{
  "userId": 1,
  "productId": 1,
  "quantity": 2
}
```

**What happens internally:**
1. OrderService calls `UserService.getUser(1)` to validate user exists
2. OrderService calls `ProductService.getProduct(1)` to validate product exists
3. OrderService calls `ProductService.getAvailableStock(1)` to check inventory
4. OrderService creates the order
5. OrderService calls `ProductService.purchaseProduct(1, 2)` to update stock

**Example:**
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 2
  }'
```

**Response:**
```json
{
  "status": "success",
  "message": "Order placed successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "productId": 1,
    "quantity": 2,
    "totalPrice": 1999.98,
    "status": "CONFIRMED",
    "createdAt": 1704384932456
  },
  "error": null
}
```

#### 3. Get Order by ID
```
GET /api/orders/{id}
```

**Example:**
```bash
curl http://localhost:8083/api/orders/1
```

#### 4. Get User's Orders
```
GET /api/orders/user/{userId}
```

**Example:**
```bash
curl http://localhost:8083/api/orders/user/1
```

#### 5. Update Order Status
```
PUT /api/orders/{id}/status
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "SHIPPED"
}
```

**Example:**
```bash
curl -X PUT http://localhost:8083/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED"}'
```

#### 6. Cancel Order
```
DELETE /api/orders/{id}
```

**Example:**
```bash
curl -X DELETE http://localhost:8083/api/orders/1
```

#### 7. Get Order Statistics
```
GET /api/orders/stats/summary
```

**Example:**
```bash
curl http://localhost:8083/api/orders/stats/summary
```

**Response:**
```json
{
  "status": "success",
  "message": "Order statistics retrieved successfully",
  "data": {
    "totalOrders": 5,
    "totalRevenue": 5000.00,
    "confirmedOrders": 4,
    "cancelledOrders": 1
  },
  "error": null
}
```

---

## Complete Demo Scenario

### Step 1: Create a User
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "phone": "+1111111111",
    "address": "789 Elm Street"
  }'
# Response: User created with ID 1
```

### Step 2: Create Products
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Keyboard",
    "description": "Mechanical keyboard",
    "price": 150.00,
    "quantity": 20
  }'
# Response: Product created with ID 1

curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Monitor",
    "description": "4K Monitor",
    "price": 400.00,
    "quantity": 5
  }'
# Response: Product created with ID 2
```

### Step 3: Place an Order (Cross-Service)
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 2
  }'
# Response: Order placed successfully
# Internally: Validates user 1, validates product 1, checks stock, creates order, updates inventory
```

### Step 4: Check Product Stock
```bash
curl http://localhost:8082/api/products/1/stock
# Expected: 18 (was 20, now 20-2=18 after order)
```

### Step 5: View Order Details
```bash
curl http://localhost:8083/api/orders/1
# Shows order with user ID 1, product ID 1, quantity 2, total $300
```

### Step 6: Get User's Orders
```bash
curl http://localhost:8083/api/orders/user/1
# Shows all orders for user 1
```

---

## Testing Tools

### Using Postman
1. Download Postman
2. Import the endpoints as shown above
3. Test each endpoint with different payloads

### Using VS Code REST Client Extension
Create a `.rest` or `.http` file:

```http
### Create User
POST http://localhost:8081/api/users
Content-Type: application/json

{
  "name": "Bob",
  "email": "bob@example.com",
  "phone": "+1234567890",
  "address": "123 Main"
}

### Get All Users
GET http://localhost:8081/api/users

### Create Product
POST http://localhost:8082/api/products
Content-Type: application/json

{
  "name": "Mouse",
  "description": "Wireless mouse",
  "price": 29.99,
  "quantity": 100
}

### Place Order
POST http://localhost:8083/api/orders
Content-Type: application/json

{
  "userId": 1,
  "productId": 1,
  "quantity": 5
}
```

---

## Key Features Demonstrated

✓ **Independent Services** - Run on different ports (8081, 8082, 8083)
✓ **REST APIs** - HTTP endpoints instead of method calls
✓ **Data Isolation** - Each service has separate repository
✓ **Cross-Service Communication** - OrderService calls UserService and ProductService
✓ **Request/Response Validation** - Proper error handling
✓ **Scalability** - Services can be deployed independently
✓ **CORS Support** - Cross-origin requests allowed

---

## Troubleshooting

**Port Already in Use:**
```bash
# On Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# On Linux/Mac
lsof -i :8081
kill -9 <PID>
```

**Service Not Responding:**
- Ensure all three services are running
- Check firewall settings
- Verify ports 8081, 8082, 8083 are open

**Order Creation Fails:**
- Make sure User Service is running (needed to validate user)
- Make sure Product Service is running (needed to validate product)
- Ensure the user and product IDs exist
- Check inventory is sufficient
