# HTTP-Based Microservices Architecture - Decoupling Guide

## Overview

This document explains how the microservices have been **decoupled** to use HTTP-based REST communication instead of direct Java object references. This makes them truly independent services that can run on separate machines and handle failures gracefully.

---

## Architecture Comparison

### BEFORE: Tightly Coupled (Direct Java References)
```
┌─────────────────────────────────────────┐
│     MicroservicesApplication            │
│     (Single JVM Process)                │
│                                         │
│  ┌──────────────┐                       │
│  │ OrderService │                       │
│  │   ↓          ↓                       │
│  │ UserService  ProductService          │
│  │  (Java obj)   (Java obj)             │
│  │                                      │
│  └──────────────┘                       │
│                                         │
│  Repositories (Shared Memory)           │
└─────────────────────────────────────────┘

PROBLEMS:
❌ Single point of failure (one process crash = everything down)
❌ Tightly coupled (cannot deploy independently)
❌ Cannot scale services individually
❌ Hard to debug (no network visibility)
❌ Not a true microservices architecture
```

### AFTER: Decoupled (HTTP REST Communication)
```
┌─────────────────┐
│  REST Client    │
│  (Port 8080)    │
└────────┬────────┘
         │
    ┌────┴────────────────┬─────────────────┐
    │                     │                 │
    ↓                     ↓                 ↓
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ User Service │  │Product Service│ │ Order Service│
│ (Port 8081)  │  │ (Port 8082)   │ │ (Port 8083)  │
│              │  │               │ │              │
│ HTTP/REST    │  │ HTTP/REST     │ │ HTTP/REST    │
│ ├ UserService│  │ ├ ProductService
│ │ (REST API) │  │ │ (REST API)  │ │ ├ OrderService
│ └─ Repo      │  │ └─ Repo       │ │ └─ Repo      │
└──────────────┘  └──────────────┘  └──────────────┘

BENEFITS:
✅ True independence (each service is separate process)
✅ Loose coupling (communicate via REST URLs only)
✅ Independent scaling (add more instances per service)
✅ Resilient (one service down doesn't crash others)
✅ True microservices architecture
✅ Network visibility and monitoring
✅ Can be deployed on different machines/containers
```

---

## Service Communication Flow

### HTTP-Based Order Processing

```
CLIENT REQUEST: Place Order for User 1, Product 1, Qty 2
│
├─ POST /api/orders
│  {
│    "userId": 1,
│    "productId": 1,
│    "quantity": 2
│  }
│
└─→ API GATEWAY (Port 8080)
     │
     └─→ ORDER SERVICE (Port 8083)
         │
         ├─ HTTP GET http://localhost:8081/api/users/1
         │  └─→ USER SERVICE (Port 8081)
         │      └─ Response: User found ✓
         │
         ├─ HTTP GET http://localhost:8082/api/products/1
         │  └─→ PRODUCT SERVICE (Port 8082)
         │      └─ Response: Product found ✓
         │
         ├─ HTTP GET http://localhost:8082/api/products/1/stock
         │  └─→ PRODUCT SERVICE (Port 8082)
         │      └─ Response: 10 units in stock ✓
         │
         ├─ Create order in local OrderRepository
         │  └─ Order ID 1 created ✓
         │
         ├─ HTTP POST http://localhost:8082/api/products/1/purchase
         │  { "quantity": 2 }
         │  └─→ PRODUCT SERVICE (Port 8082)
         │      └─ Response: Stock updated (10 - 2 = 8) ✓
         │
         └─ Return response to client
            {
              "id": 1,
              "userId": 1,
              "productId": 1,
              "quantity": 2,
              "totalPrice": 1999.98,
              "status": "CONFIRMED"
            }
```

---

## Port Independence Analysis

### Scenario 1: Port 8080 Goes Down

```
STATUS: API Gateway unavailable
├─ Port 8080: DOWN ✗
├─ Port 8081 (User Service): WORKING ✓
├─ Port 8082 (Product Service): WORKING ✓
└─ Port 8083 (Order Service): WORKING ✓

IMPACT:
- Web API gateway not available
- Cannot access services through main gateway
- BUT: Services still running independently

ACCESS METHODS:
- ❌ Cannot: POST http://localhost:8080/api/orders
- ✅ Can: POST http://localhost:8083/api/orders (direct service access)
- ✅ Can: GET http://localhost:8081/api/users (direct service access)
- ✅ Can: GET http://localhost:8082/api/products (direct service access)

SOLUTION: Route requests to individual services directly
```

### Scenario 2: Port 8081 (User Service) Goes Down

```
STATUS: User Service unavailable
├─ Port 8080 (Gateway): WORKING ✓
├─ Port 8081 (User Service): DOWN ✗
├─ Port 8082 (Product Service): WORKING ✓
└─ Port 8083 (Order Service): WORKING ✓

IMPACT ON OPERATIONS:
─────────────────────

✅ GET /api/products - WORKS
   (no User Service dependency)

✅ GET /api/orders - WORKS
   (no User Service dependency for listing)

✅ GET /api/orders/{id} - WORKS
   (no User Service dependency)

❌ POST /api/orders - FAILS
   (needs to validate user exists)

RESPONSE:
{
  "status": "error",
  "message": "User Service is unavailable",
  "data": null
}

RESILIENCE HANDLING:
- OrderService catches ServiceUnavailableException
- Returns graceful error message
- User Service failure doesn't crash Order Service
- Product Service continues working normally

WORKAROUND:
- Restart User Service on port 8081
- Or: Route to backup User Service on different port
- Or: Use cached user data (eventual consistency)
```

### Scenario 3: Port 8082 (Product Service) Goes Down

```
STATUS: Product Service unavailable
├─ Port 8080 (Gateway): WORKING ✓
├─ Port 8081 (User Service): WORKING ✓
├─ Port 8082 (Product Service): DOWN ✗
└─ Port 8083 (Order Service): WORKING ✓

IMPACT ON OPERATIONS:
─────────────────────

✅ GET /api/users - WORKS
   (no Product Service dependency)

✅ GET /api/orders - WORKS
   (returns existing orders, no service call needed)

✅ POST /api/users - WORKS
   (no Product Service dependency)

❌ POST /api/orders - FAILS
   (needs to validate product and check stock)

RESPONSE:
{
  "status": "error",
  "message": "Product Service is unavailable",
  "data": null
}

RESILIENCE HANDLING:
- OrderService catches ServiceUnavailableException
- Returns graceful error message
- Product Service failure doesn't crash Order Service
- User Service continues working normally

WORKAROUND:
- Restart Product Service on port 8082
- Or: Route to backup Product Service on different port
- Or: Use cached product data with queue for async processing
```

### Scenario 4: Port 8083 (Order Service) Goes Down

```
STATUS: Order Service unavailable
├─ Port 8080 (Gateway): WORKING ✓
├─ Port 8081 (User Service): WORKING ✓
├─ Port 8082 (Product Service): WORKING ✓
└─ Port 8083 (Order Service): DOWN ✗

IMPACT ON OPERATIONS:
─────────────────────

✅ GET /api/users - WORKS
   (independent service)

✅ GET /api/users/{id} - WORKS
   (independent service)

✅ POST /api/users - WORKS
   (independent service)

✅ GET /api/products - WORKS
   (independent service)

✅ GET /api/products/{id} - WORKS
   (independent service)

✅ POST /api/products - WORKS
   (independent service)

❌ GET /api/orders - FAILS
   (Order Service unavailable)

❌ POST /api/orders - FAILS
   (Order Service unavailable)

RESILIENCE:
- User Service works independently ✓
- Product Service works independently ✓
- Order Service failure doesn't affect other services
- Can restart Order Service independently

WORKAROUND:
- Restart Order Service on port 8083
- Or: Deploy Order Service on different port (e.g., 8084)
- Or: Use load balancer to distribute Order Service instances
```

---

## Implementation Details

### HTTP Client Classes

#### 1. `ServiceConfig.java`
Centralized configuration for service URLs and timeouts.

```java
@Component
public class ServiceConfig {
    @Value("${service.user.url:http://localhost:8081}")
    private String userServiceUrl;
    
    @Value("${service.product.url:http://localhost:8082}")
    private String productServiceUrl;
    
    // Configurable in application.yml
}
```

#### 2. `UserServiceClient.java`
HTTP client for calling User Service REST endpoints.

```java
@Component
public class UserServiceClient {
    
    public User getUserById(int userId) {
        // Makes HTTP GET call to User Service
        // GET http://localhost:8081/api/users/{userId}
    }
    
    public boolean userExists(int userId) {
        // Validates user existence
    }
}
```

#### 3. `ProductServiceClient.java`
HTTP client for calling Product Service REST endpoints.

```java
@Component
public class ProductServiceClient {
    
    public Product getProductById(int productId) {
        // Makes HTTP GET call to Product Service
        // GET http://localhost:8082/api/products/{productId}
    }
    
    public int getAvailableStock(int productId) {
        // Makes HTTP GET call to check stock
        // GET http://localhost:8082/api/products/{productId}/stock
    }
    
    public boolean purchaseProduct(int productId, int quantity) {
        // Makes HTTP POST call to update stock
        // POST http://localhost:8082/api/products/{productId}/purchase
    }
}
```

#### 4. Exception Classes

```java
// Thrown when service is unreachable
ServiceUnavailableException extends RuntimeException

// Thrown when there's a communication error
ServiceCommunicationException extends RuntimeException
```

### Updated OrderService

The `OrderService` now uses HTTP clients instead of direct references:

```java
@Service
public class OrderService {
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private ProductServiceClient productServiceClient;
    
    public Order placeOrder(int userId, int productId, int quantity) {
        // Call User Service via HTTP
        User user = userServiceClient.getUserById(userId);
        
        // Call Product Service via HTTP
        Product product = productServiceClient.getProductById(productId);
        
        // Call Product Service via HTTP for stock check
        int stock = productServiceClient.getAvailableStock(productId);
        
        // Create order
        Order order = orderRepository.createOrder(...);
        
        // Call Product Service via HTTP to update stock
        productServiceClient.purchaseProduct(productId, quantity);
        
        return order;
    }
}
```

---

## Configuration

### application.yml

```yaml
service:
  user:
    url: http://localhost:8081
  product:
    url: http://localhost:8082
  order:
    url: http://localhost:8083
  timeout:
    connect: 5000  # 5 seconds
    read: 10000    # 10 seconds
```

This allows easy reconfiguration without code changes:
- Change service URLs for different environments
- Adjust timeouts based on network conditions
- Support service discovery integration

---

## Error Handling & Resilience

### Timeout Handling

```
Connection Timeout: 5 seconds
├─ If service takes >5s to connect
├─ Request abandoned
└─ ServiceUnavailableException thrown

Read Timeout: 10 seconds
├─ If service takes >10s to respond
├─ Request abandoned
└─ ServiceUnavailableException thrown
```

### Graceful Error Handling

```java
public Order placeOrder(int userId, int productId, int quantity) {
    try {
        User user = userServiceClient.getUserById(userId);
    } catch (ServiceUnavailableException e) {
        // User Service is down
        throw new IllegalArgumentException(
            "User Service temporarily unavailable. Try again later."
        );
    } catch (ServiceCommunicationException e) {
        // Network or communication error
        throw new IllegalArgumentException(
            "Error communicating with User Service. Try again."
        );
    }
}
```

---

## How to Test Port Independence

### Test 1: Port 8080 Down
```bash
# Don't start API Gateway (port 8080)
# Start individual services:
java -jar microservices.jar --server.port=8081  # User Service
java -jar microservices.jar --server.port=8082  # Product Service
java -jar microservices.jar --server.port=8083  # Order Service

# Test direct access:
curl http://localhost:8081/api/users          # ✓ Works
curl http://localhost:8082/api/products       # ✓ Works
curl http://localhost:8083/api/orders         # ✓ Works

# Result: Services work independently
```

### Test 2: Port 8081 Down (User Service)
```bash
# Start services but skip User Service:
java -jar microservices.jar --server.port=8080  # Gateway
java -jar microservices.jar --server.port=8082  # Product Service
java -jar microservices.jar --server.port=8083  # Order Service

# Test Order Creation:
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"quantity":2}'

# Result: Error (User Service unavailable)
# {
#   "status": "error",
#   "message": "User Service is unavailable"
# }

# BUT:
curl http://localhost:8080/api/products        # ✓ Works
curl http://localhost:8080/api/users           # ✗ Fails (depends on 8081)

# Result: Product Service works independently
```

### Test 3: Port 8082 Down (Product Service)
```bash
# Start services but skip Product Service:
java -jar microservices.jar --server.port=8080  # Gateway
java -jar microservices.jar --server.port=8081  # User Service
java -jar microservices.jar --server.port=8083  # Order Service

# Test Order Creation:
curl -X POST http://localhost:8080/api/orders \
  -d '{"userId":1,"productId":1,"quantity":2}'

# Result: Error (Product Service unavailable)

# BUT:
curl http://localhost:8080/api/users            # ✓ Works

# Result: User Service works independently
```

---

## Advanced: Adding Circuit Breaker Pattern

To make services even more resilient, add circuit breaker pattern:

```java
@Component
public class UserServiceClient {
    
    @CircuitBreaker(
        name = "userService",
        fallbackMethod = "getUserByIdFallback"
    )
    public User getUserById(int userId) {
        // HTTP call to User Service
    }
    
    public User getUserByIdFallback(int userId, Exception ex) {
        // Return cached data or default
        return new User(); // Cached/default user
    }
}
```

---

## Summary

| Aspect | Before (Tightly Coupled) | After (Decoupled - HTTP) |
|--------|--------------------------|--------------------------|
| **Communication** | Direct Java Objects | HTTP REST Calls |
| **Deployment** | Single JAR/Process | Separate Services |
| **Failure Impact** | One crash = all down | Independent failures |
| **Scaling** | Scale entire app | Scale per service |
| **Resilience** | No resilience | Graceful degradation |
| **Visibility** | Internal only | Network visible |
| **Port Independence** | N/A | True port independence |
| **True Microservices** | ❌ No | ✅ Yes |

---

## Next Steps

1. **Service Discovery**: Add Eureka/Consul for dynamic service registration
2. **Load Balancing**: Add Ribbon/Spring Cloud LoadBalancer
3. **Circuit Breaker**: Add Hystrix/Resilience4j for better resilience
4. **Monitoring**: Add Spring Cloud Sleuth for distributed tracing
5. **API Gateway**: Implement Spring Cloud Gateway for unified entry point
6. **Message Queue**: Add RabbitMQ/Kafka for asynchronous communication
7. **Containerization**: Dockerize each service for deployment

---

## References

- Spring Cloud Documentation
- Netflix Hystrix (Circuit Breaker)
- Resilience4j
- Spring Cloud Sleuth
- Spring Cloud Gateway
